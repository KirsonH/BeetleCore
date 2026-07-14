import { BissMessage } from '../../contracts/biss';

interface PortalBalanceProps {
  sampleMessage: BissMessage;
}

export default function PortalBalance({ sampleMessage }: PortalBalanceProps) {
  return (
    <div className="rounded-3xl border border-slate-800 bg-slate-900/85 p-6 shadow-xl shadow-slate-950/20">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-sm uppercase tracking-[0.24em] text-slate-500">Customer portal</p>
          <h2 className="text-3xl font-semibold text-slate-100">Live balance snapshot</h2>
          <p className="mt-2 max-w-2xl text-slate-400">Read-only ledger experience for the active tenant and book context.</p>
        </div>
        <div className="rounded-3xl bg-slate-950/80 px-5 py-4 text-right text-sm text-slate-200">
          <p className="font-semibold">Currency</p>
          <p>{sampleMessage.context.systemCurrency}</p>
        </div>
      </div>

      <div className="mt-8 grid gap-4 md:grid-cols-3">
        <div className="rounded-3xl bg-slate-950/80 p-5">
          <p className="text-xs uppercase tracking-[0.24em] text-slate-500">Account</p>
          <p className="mt-3 text-2xl font-semibold text-slate-100">{sampleMessage.body.accountId ?? 'N/A'}</p>
        </div>
        <div className="rounded-3xl bg-slate-950/80 p-5">
          <p className="text-xs uppercase tracking-[0.24em] text-slate-500">Balance</p>
          <p className="mt-3 text-2xl font-semibold text-slate-100">{sampleMessage.body.amount ?? 0} {sampleMessage.body.currency}</p>
        </div>
        <div className="rounded-3xl bg-slate-950/80 p-5">
          <p className="text-xs uppercase tracking-[0.24em] text-slate-500">Location</p>
          <p className="mt-3 text-2xl font-semibold text-slate-100">{sampleMessage.context.geolocation.country}</p>
        </div>
      </div>
    </div>
  );
}
