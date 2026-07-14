import { useMemo, useState } from 'react';
import { BissMessage } from '../../contracts/biss';

interface ProductForgeProps {
  sampleMessage: BissMessage;
}

export default function ProductForge({ sampleMessage }: ProductForgeProps) {
  const [productName, setProductName] = useState('Open Banking Savings');
  const [locale, setLocale] = useState(sampleMessage.context.systemLocale);
  const [maxAmount, setMaxAmount] = useState(15000);
  const [productTrait, setProductTrait] = useState('embedded-credit');

  const bundle = useMemo(
    () => ({
      productName,
      locale,
      traits: [productTrait],
      limits: {
        max_transaction_amount: maxAmount,
        currency: sampleMessage.context.systemCurrency,
      },
      author: sampleMessage.header().system(),
      tenantContext: {
        tenantId: sampleMessage.context.tenantId(),
        branchId: sampleMessage.context.branchId(),
        bookId: sampleMessage.context.bookId(),
      },
    }),
    [locale, maxAmount, productName, productTrait, sampleMessage],
  );

  return (
    <div className="space-y-8">
      <section className="rounded-3xl border border-slate-800 bg-slate-900/85 p-6 shadow-xl shadow-slate-950/20">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <p className="text-sm uppercase tracking-[0.24em] text-slate-500">Product Forge Control</p>
            <h2 className="text-3xl font-semibold text-slate-100">Build declarative rule bundles</h2>
            <p className="mt-2 max-w-2xl text-slate-400">Create and preview Open Banking product rule bundles before they are sent to the backend rule engine.</p>
          </div>
          <button className="rounded-full bg-sky-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-sky-500">
            Publish bundle
          </button>
        </div>
      </section>

      <div className="grid gap-4 lg:grid-cols-2">
        <div className="rounded-3xl border border-slate-800 bg-slate-950/80 p-6">
          <label className="mb-4 block text-sm text-slate-400">Product Name</label>
          <input
            className="w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 outline-none"
            value={productName}
            onChange={(event) => setProductName(event.target.value)}
          />

          <label className="mt-6 block text-sm text-slate-400">Locale</label>
          <input
            className="w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 outline-none"
            value={locale}
            onChange={(event) => setLocale(event.target.value)}
          />

          <label className="mt-6 block text-sm text-slate-400">Max Transaction Amount</label>
          <input
            type="number"
            className="w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 outline-none"
            value={maxAmount}
            onChange={(event) => setMaxAmount(Number(event.target.value))}
          />

          <label className="mt-6 block text-sm text-slate-400">Product Trait</label>
          <select
            className="w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 outline-none"
            value={productTrait}
            onChange={(event) => setProductTrait(event.target.value)}
          >
            <option value="embedded-credit">embedded-credit</option>
            <option value="savings-account">savings-account</option>
            <option value="payment-facility">payment-facility</option>
          </select>
        </div>

        <div className="rounded-3xl border border-slate-800 bg-slate-950/80 p-6">
          <p className="text-sm uppercase tracking-[0.24em] text-slate-500">Rule Bundle Preview</p>
          <pre className="mt-4 max-h-[480px] overflow-auto rounded-2xl bg-slate-900 p-4 text-sm leading-6 text-slate-200">
            {JSON.stringify(bundle, null, 2)}
          </pre>
        </div>
      </div>
    </div>
  );
}
