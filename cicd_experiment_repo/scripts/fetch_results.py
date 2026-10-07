#!/usr/bin/env python3
"""Turn run_ids.csv into raw.csv (one row per run) using the GitHub CLI.

Usage: python3 scripts/fetch_results.py owner/repo [run_ids.csv] [raw.csv]

Per run it records:
  wall_s    run_started_at -> updated_at   (end-to-end feedback time, queue time excluded)
  runner_s  sum of job durations, skipped jobs count as 0   (compute consumed)
  ttf_s     run_started_at -> completion of first failed step (failure scenario only)
  conclusion success / failure
"""
import csv, json, subprocess, sys
from datetime import datetime

def ts(s):
    return datetime.fromisoformat(s.replace("Z", "+00:00")) if s else None

def gh(path):
    out = subprocess.run(["gh", "api", path], check=True, capture_output=True, text=True).stdout
    return json.loads(out)

def summarize(run, jobs):
    """Pure function (testable offline): GitHub API objects -> metrics dict."""
    start = ts(run["run_started_at"])
    wall = (ts(run["updated_at"]) - start).total_seconds()
    runner, first_fail = 0.0, None
    for j in jobs:
        if j.get("conclusion") == "skipped" or not j.get("started_at") or not j.get("completed_at"):
            continue
        runner += (ts(j["completed_at"]) - ts(j["started_at"])).total_seconds()
        for s in j.get("steps", []):
            if s.get("conclusion") == "failure" and s.get("completed_at"):
                t = (ts(s["completed_at"]) - start).total_seconds()
                first_fail = t if first_fail is None else min(first_fail, t)
    return {"wall_s": round(wall, 1), "runner_s": round(runner, 1),
            "ttf_s": "" if first_fail is None else round(first_fail, 1),
            "conclusion": run.get("conclusion")}

def main():
    repo = sys.argv[1]
    src = sys.argv[2] if len(sys.argv) > 2 else "run_ids.csv"
    dst = sys.argv[3] if len(sys.argv) > 3 else "raw.csv"
    rows = []
    for r in csv.DictReader(open(src)):
        run = gh(f"repos/{repo}/actions/runs/{r['run_id']}")
        jobs = gh(f"repos/{repo}/actions/runs/{r['run_id']}/jobs?per_page=100")["jobs"]
        m = summarize(run, jobs)
        rows.append({**r, **m})
        print(r["workflow"], r["scenario"], r["round"], m)
    with open(dst, "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        w.writeheader(); w.writerows(rows)
    print("wrote", dst)

if __name__ == "__main__":
    main()
