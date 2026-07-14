import { BissMessage } from '../../contracts/biss';

interface DashboardProps {
  sampleMessage: BissMessage;
}

export default function Dashboard({ sampleMessage }: DashboardProps) {
  const ledgerEvents = Array.isArray(sampleMessage.body.lineageChain)
    ? sampleMessage.body.lineageChain as string[]
    : [];

  return (
    <div className="space-y-8">
      <section className="rounded-3xl border border-slate-800 bg-slate-900/85 p-6 shadow-xl shadow-slate-950/20">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div className="rounded-2xl bg-slate-950/80 p-5">
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500">Tenant</p>
            <p className="mt-3 text-2xl font-semibold text-slate-100">{sampleMessage.context.tenantId}</p>
          </div>
          <div className="rounded-2xl bg-slate-950/80 p-5">
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500">Branch</p>
            <p className="mt-3 text-2xl font-semibold text-slate-100">{sampleMessage.context.branchId}</p>
          </div>
          <div className="rounded-2xl bg-slate-950/80 p-5">
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500">Book</p>
            <p className="mt-3 text-2xl font-semibold text-slate-100">{sampleMessage.context.bookId}</p>
          </div>
          <div className="rounded-2xl bg-slate-950/80 p-5">
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500">Locale</p>
            <p className="mt-3 text-2xl font-semibold text-slate-100">{sampleMessage.context.systemLocale}</p>
          </div>
        </div>
      </section>

      <section className="rounded-3xl border border-slate-800 bg-slate-900/85 p-6 shadow-xl shadow-slate-950/20">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <p className="text-sm uppercase tracking-[0.24em] text-slate-500">Immutable Ledger Log</p>
            <h2 className="text-3xl font-semibold text-slate-100">Cross-dimensional audit stream</h2>
          </div>
          <div className="rounded-full bg-slate-950/80 px-4 py-2 text-sm text-slate-200">
            Sandbox mode: {sampleMessage.header.isSandbox ? 'Enabled' : 'Disabled'}
          </div>
        </div>

        <div className="mt-6 overflow-hidden rounded-3xl border border-slate-800 bg-slate-950/90">
          <div className="grid grid-cols-[1fr_1fr_1fr_1fr] gap-0 border-b border-slate-800 bg-slate-900/80 px-6 py-4 text-xs uppercase tracking-[0.24em] text-slate-500">
            <span>Event</span>
            <span>Operator</span>
            <span>Session</span>
            <span>Region</span>
          </div>
          <div className="divide-y divide-slate-800">
            {ledgerEvents.length === 0 ? (
              <div className="px-6 py-6 text-slate-400">No lineage events available.</div>
            ) : ledgerEvents.map((eventId) => (
              <div key={eventId} className="grid grid-cols-[1fr_1fr_1fr_1fr] gap-0 px-6 py-4 text-slate-200 hover:bg-slate-900/60">
                <span className="font-medium text-slate-100">{eventId}</span>
                <span>{sampleMessage.audit.operatorId}</span>
                <span>{sampleMessage.audit.clientSessionId}</span>
                <span>{sampleMessage.context.geolocation.region}</span>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
