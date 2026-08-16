# Phase 14 final verification

Phase 14 is ready for review when the deployment runbook, local demo evidence,
portfolio case study, and repository checks all pass on the same commit.

## Documentation and repository gate

From the repository root:

~~~powershell
git diff --check
.\scripts\verify-phase-14.ps1 -SkipDocker
~~~

## Local stack gate

On a machine with Docker Desktop running:

~~~powershell
.\scripts\verify-phase-14-demo.ps1
~~~

The command starts from the existing Compose environment and verifies the web,
control-plane, and intelligence-service endpoints. For a clean demonstration:

~~~powershell
.\scripts\run-phase-14-demo.ps1 -Rebuild -OpenEndpoints
~~~

## Review checklist

- [x] Phase 13 checks remain green on the reviewed base commit.
- [x] Compose configuration, image builds, startup ordering, and health checks pass.
- [x] The screenshot sequence in docs/phase-14-demo-evidence.md is complete.
- [x] Published screenshots contain no credentials, tokens, private data, or local paths.
- [x] The portfolio project link points to this repository and the overview image is current.
- [x] The case study states the local-demo and non-production limitations.
- [x] git diff --check is clean.
- [x] The pull request checks are green before merge.

## Release marker

After the pull request is merged and the final verification output is recorded,
create an annotated demonstration tag if desired:

~~~powershell
git switch main
git pull --ff-only
git tag -a phase-14.0.0-demo -m "Phase 14 deployment and portfolio demo"
git push origin phase-14.0.0-demo
~~~

The tag identifies the reviewed demonstration state. It is not a production
release and does not imply public deployment.
