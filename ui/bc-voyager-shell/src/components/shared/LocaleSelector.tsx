interface LocaleSelectorProps {
  locale: string;
  onLocaleChange: (nextLocale: string) => void;
}

const locales = [
  { value: 'en-US', label: 'English (US)' },
  { value: 'es-EC', label: 'Español (EC)' },
  { value: 'pt-BR', label: 'Português (BR)' },
];

export default function LocaleSelector({ locale, onLocaleChange }: LocaleSelectorProps) {
  return (
    <label className="flex items-center gap-3 rounded-full border border-slate-700 bg-slate-900 px-4 py-2 text-sm text-slate-200">
      <span>Locale</span>
      <select
        className="rounded bg-slate-950 px-2 py-1 text-slate-100 outline-none"
        value={locale}
        onChange={(event) => onLocaleChange(event.target.value)}
      >
        {locales.map((option) => (
          <option key={option.value} value={option.value} className="bg-slate-950 text-slate-100">
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}
