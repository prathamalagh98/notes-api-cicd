#!/usr/bin/env bash
# Dispatches the experiment runs one at a time (never in parallel, so runs do not
# compete for the shared cache/network) and records every run id in run_ids.csv.
#
# Usage:  ./run_experiment.sh owner/repo
# Needs:  GitHub CLI (`gh auth login` done), a public repo (free Actions minutes)
# Env:    GREEN_RUNS (default 11)  FAIL_RUNS (default 5)
set -euo pipefail

REPO="${1:?usage: ./run_experiment.sh owner/repo}"
GREEN_RUNS="${GREEN_RUNS:-11}"
FAIL_RUNS="${FAIL_RUNS:-5}"
WORKFLOWS=(ci-a-baseline.yml ci-b-cached.yml ci-c-staged.yml)
OUT="run_ids.csv"
[ -f "$OUT" ] || echo "workflow,scenario,round,run_id" > "$OUT"

latest_id() {
  gh run list -R "$REPO" --workflow "$1" --limit 1 --json databaseId -q '.[0].databaseId // 0'
}

dispatch_and_wait() {
  local wf="$1" inject="$2" scenario="$3" round="$4" before id
  before=$(latest_id "$wf")
  gh workflow run "$wf" -R "$REPO" -f inject_failure="$inject"
  id="$before"
  for _ in $(seq 1 40); do
    sleep 3
    id=$(latest_id "$wf")
    [ "$id" != "$before" ] && break
  done
  if [ "$id" = "$before" ]; then echo "run for $wf did not appear" >&2; exit 1; fi
  echo "[$scenario #$round] $wf -> run $id (waiting)"
  gh run watch "$id" -R "$REPO" >/dev/null 2>&1 || true   # a red run is expected in the failure scenario
  echo "$wf,$scenario,$round,$id" >> "$OUT"
}

# Round-robin over the configurations so that time-of-day / runner-fleet drift
# affects all configurations equally.
for round in $(seq 1 "$GREEN_RUNS"); do
  for wf in "${WORKFLOWS[@]}"; do dispatch_and_wait "$wf" false green "$round"; done
done
for round in $(seq 1 "$FAIL_RUNS"); do
  for wf in "${WORKFLOWS[@]}"; do dispatch_and_wait "$wf" true failure "$round"; done
done
echo "Done. Next: python3 scripts/fetch_results.py $REPO"
