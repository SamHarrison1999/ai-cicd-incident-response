# Phase 11 end-to-end scenarios

1. An authenticated member queries feedback for an organisation and project and receives bounded aggregate windows.
2. A policy-version or date filter returns only matching aggregate windows in deterministic order.
3. A small sample is returned with an explicit suppression reason and is not presented as an actionable provider signal.
4. A user without tenant membership cannot query another organisation's feedback.
5. The workspace displays empty, loading, error, and suppressed states without exposing raw review or evidence content.
6. No feedback action changes provider configuration, production policy, or incident state.
