export function SettingsPage() {
    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Configuration</p>
                <h2>Settings</h2>
                <p>
                    Organisation, project, event-source, and provider settings
                    will be added through their respective phases.
                </p>
            </section>
            <section className="settings-panel">
                <h3>Safety baseline</h3>
                <dl className="definition-list">
                    <div>
                        <dt>Automatic remediation</dt>
                        <dd>Disabled</dd>
                    </div>
                    <div>
                        <dt>Human review</dt>
                        <dd>Required</dd>
                    </div>
                    <div>
                        <dt>AI provider</dt>
                        <dd>Deterministic offline mode</dd>
                    </div>
                </dl>
            </section>
        </>
    );
}
