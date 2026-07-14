import { BissMessage } from '../../contracts/biss';

interface ProductForgeProps {
  sampleMessage: BissMessage;
}

export default function ProductForge({ sampleMessage }: ProductForgeProps) {
  return (
    <div className="rounded-3xl border border-slate-800 bg-slate-900/85 p-6 shadow-xl shadow-slate-950/20">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <p className="text-sm uppercase tracking-[0.24em] text-slate-500">Product Forge</p>
          <h2 className="text-3xl font-semibold text-slate-100">Atomic service composition</h2>
          <p className="mt-2 max-w-2xl text-slate-400">Prototype new ledger-aware services using the current BISS context.</p>
        </div>
        <div className="rounded-full bg-slate-950/80 px-4 py-2 text-sm text-slate-200">Sandbox mode</div>
      </div>

      <div className="mt-8 grid gap-4 md:grid-cols-2">
        <div className="rounded-3xl bg-slate-950/80 p-5">
          <p className="text-xs uppercase tracking-[0.24em] text-slate-500">Transaction</p>
          <p className="mt-3 text-2xl font-semibold text-slate-100">{sampleMessage.header.transactionCode}</p>
        </div>
        <div className="rounded-3xl bg-slate-950/80 p-5">
          <p className="text-xs uppercase tracking-[0.24em] text-slate-500">Operator</p>
          <p className="mt-3 text-2xl font-semibold text-slate-100">{sampleMessage.audit.operatorId}</p>
        </div>
      </div>

      <div className="mt-8 rounded-3xl bg-slate-950/80 p-5 text-slate-300">
        <p className="text-sm uppercase tracking-[0.24em] text-slate-500">Message payload</p>
        <pre className="mt-4 overflow-x-auto text-xs leading-6 text-slate-200">{JSON.stringify(sampleMessage.body, null, 2)}</pre>
      </div>
    </div>
  );
}
