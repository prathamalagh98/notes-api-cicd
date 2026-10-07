#!/usr/bin/env python3
"""raw.csv -> summary tables for the paper + fig2_feedback_time.png

Usage: python3 scripts/analyze.py [raw.csv]

Rules (stated in the paper's methodology):
  * green scenario: round 1 is a warm-up/cold-cache run, reported separately;
    statistics use rounds 2..N.
  * failure scenario: all rounds are used (caches already warm).
  * 95% CI of the mean: percentile bootstrap, 10,000 resamples, fixed seed.
"""
import csv, random, statistics as st, sys
from collections import defaultdict

NAMES = {"ci-a-baseline.yml": "A (baseline)", "ci-b-cached.yml": "B (cached)", "ci-c-staged.yml": "C (cached + staged)"}
ORDER = list(NAMES)

def boot_ci(xs, n=10000, seed=42):
    rnd = random.Random(seed)
    means = sorted(sum(rnd.choices(xs, k=len(xs))) / len(xs) for _ in range(n))
    return means[int(0.025 * n)], means[int(0.975 * n)]

def load(path):
    d = defaultdict(list)
    for r in csv.DictReader(open(path)):
        d[(r["workflow"], r["scenario"])].append(r)
    for k in d:
        d[k].sort(key=lambda r: int(r["round"]))
    return d

def stats(xs):
    lo, hi = boot_ci(xs)
    return dict(n=len(xs), mean=st.mean(xs), sd=st.stdev(xs) if len(xs) > 1 else 0.0,
                med=st.median(xs), lo=lo, hi=hi)

def main():
    d = load(sys.argv[1] if len(sys.argv) > 1 else "raw.csv")
    base = None
    print("\nTABLE V (green builds, warm runs; seconds)")
    print("Config | n | Median | Mean ± SD | 95% CI of mean | Runner time (mean) | Change vs A (median)")
    summary = {}
    for wf in ORDER:
        rows = [r for r in d[(wf, "green")][1:] if r["conclusion"] == "success"]
        if not rows: continue
        s = stats([float(r["wall_s"]) for r in rows])
        rt = st.mean(float(r["runner_s"]) for r in rows)
        summary[wf] = s
        if base is None: base = s["med"]
        chg = (s["med"] - base) / base * 100
        print(f"{NAMES[wf]} | {s['n']} | {s['med']:.0f} | {s['mean']:.0f} ± {s['sd']:.0f} | [{s['lo']:.0f}, {s['hi']:.0f}] | {rt:.0f} | {chg:+.1f}%")
    print("\nFirst (cold-cache) run per configuration, seconds")
    for wf in ORDER:
        rows = d[(wf, "green")]
        if rows: print(f"{NAMES[wf]}: wall {rows[0]['wall_s']} s, conclusion {rows[0]['conclusion']}")
    print("\nTABLE VI (injected unit-test failure; seconds)")
    print("Config | n | Time to failure signal (median) | Mean ± SD | Runner time consumed (mean)")
    base_rt = None
    for wf in ORDER:
        rows = [r for r in d[(wf, "failure")] if r["conclusion"] == "failure" and r["ttf_s"] != ""]
        if not rows: continue
        s = stats([float(r["ttf_s"]) for r in rows])
        rt = st.mean(float(r["runner_s"]) for r in rows)
        if base_rt is None: base_rt = rt
        print(f"{NAMES[wf]} | {s['n']} | {s['med']:.0f} | {s['mean']:.0f} ± {s['sd']:.0f} | {rt:.0f} ({(rt-base_rt)/base_rt*100:+.1f}% vs A)")
    try:
        import matplotlib; matplotlib.use("Agg")
        import matplotlib.pyplot as plt
        wfs = [w for w in ORDER if w in summary]
        fig, ax = plt.subplots(figsize=(3.4, 2.3), dpi=300)
        means = [summary[w]["mean"] for w in wfs]
        errs = [[summary[w]["mean"] - summary[w]["lo"] for w in wfs], [summary[w]["hi"] - summary[w]["mean"] for w in wfs]]
        ax.bar(range(len(wfs)), means, yerr=errs, capsize=3, color=["#7f7f7f", "#4c78a8", "#2f5d8a"][:len(wfs)])
        ax.set_xticks(range(len(wfs))); ax.set_xticklabels(["A", "B", "C"][:len(wfs)])
        ax.set_ylabel("Feedback time (s)"); ax.set_xlabel("Pipeline configuration")
        ax.tick_params(labelsize=7); ax.yaxis.label.set_size(8); ax.xaxis.label.set_size(8)
        fig.tight_layout(); fig.savefig("fig2_feedback_time.png"); print("\nwrote fig2_feedback_time.png")
    except ImportError:
        print("\n(matplotlib not installed: pip install matplotlib to get the figure)")

if __name__ == "__main__":
    main()
