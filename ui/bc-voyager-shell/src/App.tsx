import { useMemo, useState } from 'react';
import { BissMessage } from './contracts/biss';
import Dashboard from './components/backoffice/Dashboard';
import PortalBalance from './components/portal/PortalBalance';
import ProductForge from './components/forge/ProductForge';
import LocaleSelector from './components/shared/LocaleSelector';
import RestrictedLayout from './components/shared/RestrictedLayout';

function App() {
  const [view, setView] = useState<'portal' | 'backoffice' | 'forge'>('backoffice');
  const [locale, setLocale] = useState('en-US');
  const [tenantId, setTenantId] = useState('tenant-001');
  const [branchId, setBranchId] = useState('branch-01');
  const [bookId, setBookId] = useState('book-01');
  const [isAuthenticated, setAuthenticated] = useState(true);

  const sampleMessage: BissMessage = useMemo(
    () => ({
      header: {
        token: 'sandbox-token',
        transactionCode: 'LEDGER_APPEND',
        system: 'bc-voyager-shell',
        isSandbox: true,
        openBankingConsentId: 'consent-0001',
        authorizationToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.sample.payload',
      },
      context: {
        tenantId,
        branchId,
        bookId,
        systemLocale: locale,
        systemTimezone: 'America/Guayaquil',
        systemCurrency: 'USD',
        geolocation: {
          latitude: -0.1807,
          longitude: -78.4678,
          country: 'EC',
          region: 'Pichincha',
        },
        dataJurisdiction: 'EC',
      },
      audit: {
        operatorId: 'operator-999',
        clientSessionId: 'session-12345',
      },
      body: {
        accountId: '0001',
        amount: 1200.5,
        currency: 'USD',
        lineageChain: ['evt-001', 'evt-002', 'evt-003'],
      },
    }),
    [branchId, bookId, locale, tenantId],
  );

  const description =
    view === 'portal'
      ? 'Customer portal with real-time balance and ledger status.'
      : view === 'forge'
      ? 'Enterprise product forge for experiments and atomic schemas.'
      : 'Backoffice dashboard for cross-dimensional audit, tenant tracking, and ledger visualization.';

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <header className="border-b border-slate-800 bg-slate-900/95 px-6 py-5 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500">BeetleCore</p>
            <h1 className="text-3xl font-semibold">BC Voyager Shell</h1>
            <p className="mt-2 max-w-2xl text-slate-400">A sandboxed enterprise dashboard for immutable ledger and context-aware operations.</p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-wrap items-center gap-2">
              <button className="rounded border border-slate-700 bg-slate-800 px-4 py-2 text-sm text-slate-200 transition hover:bg-slate-700" onClick={() => setView('portal')}>
                Portal
              </button>
              <button className="rounded border border-slate-700 bg-slate-800 px-4 py-2 text-sm text-slate-200 transition hover:bg-slate-700" onClick={() => setView('backoffice')}>
                Backoffice
              </button>
              <button className="rounded border border-slate-700 bg-slate-800 px-4 py-2 text-sm text-slate-200 transition hover:bg-slate-700" onClick={() => setView('forge')}>
                Product Forge
              </button>
            </div>
            <div className="flex flex-col gap-3 sm:items-center sm:flex-row">
              <LocaleSelector locale={locale} onLocaleChange={setLocale} />
              <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                <label className="rounded-full border border-slate-700 bg-slate-800 px-4 py-2 text-sm text-slate-200">
                  Tenant
                  <select className="ml-2 bg-slate-900 text-slate-100 outline-none" value={tenantId} onChange={(event) => setTenantId(event.target.value)}>
                    <option value="tenant-001">tenant-001</option>
                    <option value="tenant-002">tenant-002</option>
                    <option value="tenant-003">tenant-003</option>
                  </select>
                </label>
                <label className="rounded-full border border-slate-700 bg-slate-800 px-4 py-2 text-sm text-slate-200">
                  Branch
                  <select className="ml-2 bg-slate-900 text-slate-100 outline-none" value={branchId} onChange={(event) => setBranchId(event.target.value)}>
                    <option value="branch-01">branch-01</option>
                    <option value="branch-02">branch-02</option>
                  </select>
                </label>
                <label className="rounded-full border border-slate-700 bg-slate-800 px-4 py-2 text-sm text-slate-200">
                  Book
                  <select className="ml-2 bg-slate-900 text-slate-100 outline-none" value={bookId} onChange={(event) => setBookId(event.target.value)}>
                    <option value="book-01">book-01</option>
                    <option value="book-02">book-02</option>
                  </select>
                </label>
                <div className="rounded-full border border-slate-700 bg-slate-800 px-4 py-2 text-sm text-slate-200">
                  Locale: {locale}
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-6 py-8">
        <section className="mb-8 rounded-3xl border border-slate-800 bg-slate-900/80 p-6 shadow-xl shadow-slate-950/20">
          <p className="text-sm uppercase tracking-[0.3em] text-slate-500">{description}</p>
        </section>

        <RestrictedLayout isAuthenticated={isAuthenticated} onLogin={() => setAuthenticated(true)} onLogout={() => setAuthenticated(false)}>
          {view === 'portal' ? (
            <PortalBalance sampleMessage={sampleMessage} />
          ) : view === 'forge' ? (
            <ProductForge sampleMessage={sampleMessage} />
          ) : (
            <Dashboard sampleMessage={sampleMessage} />
          )}
        </RestrictedLayout>
      </main>
    </div>
  );
}

export default App;
