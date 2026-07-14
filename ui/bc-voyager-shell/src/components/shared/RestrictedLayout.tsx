interface RestrictedLayoutProps {
  isAuthenticated: boolean;
  onLogin: () => void;
  onLogout: () => void;
  children: React.ReactNode;
}

export default function RestrictedLayout({ isAuthenticated, onLogin, onLogout, children }: RestrictedLayoutProps) {
  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 rounded-3xl border border-slate-800 bg-slate-900/80 p-6 shadow-xl shadow-slate-950/20 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-lg font-semibold text-slate-100">Secure Access</h2>
          <p className="mt-1 text-slate-400">Authentication gates are enforced before any ledger or audit interaction.</p>
        </div>
        <button
          className="rounded-full bg-slate-800 px-4 py-2 text-sm text-slate-200 transition hover:bg-slate-700"
          onClick={isAuthenticated ? onLogout : onLogin}
        >
          {isAuthenticated ? 'Sign out' : 'Sign in'}
        </button>
      </div>

      {isAuthenticated ? children : (
        <div className="rounded-3xl border border-amber-500/20 bg-amber-500/5 p-6 text-amber-100 shadow-inner shadow-amber-900/10">
          <p className="text-lg font-medium">Authentication required to view the enterprise ledger shell.</p>
          <p className="mt-2 text-slate-400">Use the sign in button to enable secure backoffice operations.</p>
        </div>
      )}
    </div>
  );
}
