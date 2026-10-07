# notes-api: subject of the CI/CD pipeline experiment

A small Spring Boot 3.5 REST service (Java 17, Maven, MySQL 8) plus the three GitHub Actions
pipelines and the scripts used to measure them. 18 unit tests (`@Tag("unit")`) and
10 integration tests (`@Tag("integration")`).

NOTE: the code was syntax-checked but NOT compiled or run by the author of this kit (no Maven access
in the environment where it was written). The first CI run may reveal small errors; fix them,
then freeze the commit before starting the experiment.

## Pipelines (.github/workflows)
| File | Config | What it does |
|---|---|---|
| ci-a-baseline.yml | A | one job: unit tests, integration tests, docker build; no cache |
| ci-b-cached.yml | B | same order as A + Maven cache + Docker layer cache |
| ci-c-staged.yml | C | caches as B; unit job first, then integration and image jobs in parallel |

## Steps
1. Create an empty **public** GitHub repo, put all these files in it, push to `main`
   (workflows can only be dispatched from the default branch).
2. GitHub, Actions tab: run each workflow once by hand (inject_failure = false) and fix anything red.
   Typical fixes: a test, the DB env vars, an action version (bump to current majors if warned).
3. Install the GitHub CLI and run `gh auth login`. On Windows use Git Bash or WSL for the .sh script.
4. **Do not push any more commits.** Then:

       ./scripts/run_experiment.sh owner/repo      # 48 runs, takes several hours, keep the laptop awake
       python3 scripts/fetch_results.py owner/repo # -> raw.csv
       pip install matplotlib                      # for the figure
       python3 scripts/analyze.py raw.csv          # prints Table V / VI numbers + fig2_feedback_time.png

5. Copy the printed numbers into the yellow placeholders of the paper; insert the figure; remove the
   highlight. Write the discussion from what the numbers really show (if C is not faster than B, say so).
6. Keep raw.csv and run_ids.csv in the repo: they are the evidence for the paper.

## Run locally (optional)
    docker run -d -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=testdb -p 3306:3306 mysql:8.0
    mvn test -Dgroups=unit
    mvn test -Dgroups=integration
    mvn spring-boot:run     # then: curl localhost:8080/api/notes
