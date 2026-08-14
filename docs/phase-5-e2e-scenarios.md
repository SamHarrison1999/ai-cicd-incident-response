# Phase 5 synthetic end-to-end scenarios

These scenarios describe deterministic technical checks for the Phase 5
incident-correlation path. They use synthetic events and local services.

## Scenario 1: failure to incident projection

1. Submit a valid provider-neutral failure event inside an organisation and
   project boundary.
2. Apply the versioned deterministic correlation policy.
3. Persist the bounded correlation decision and incident association.
4. Retrieve incidents through the tenant-scoped API.
5. Confirm that the response contains status, title, bounded summary, and
   lifecycle timestamps only.

Expected result: the eligible candidate is selected deterministically and the
incident projection is visible to an active member of the same organisation.

## Scenario 2: duplicate decision

1. Process the same normalised event identity twice.
2. Compare the persisted decision identity and incident association.

Expected result: the second processing attempt does not create a second
decision or a second primary incident association.

## Scenario 3: tenant and role boundaries

1. Request an incident using a different organisation or project identifier.
2. Request a lifecycle transition as a viewer.
3. Request an invalid lifecycle transition as an allowed writer.

Expected result: cross-boundary resources are not disclosed, viewers cannot
change state, and invalid transitions leave the aggregate unchanged.

## Evidence and exclusions

The Java, frontend, repository, and Compose verification commands are the
recorded evidence for this batch. These scenarios do not claim production
integration, causal certainty, improved reliability, human review outcomes,
or autonomous remediation.