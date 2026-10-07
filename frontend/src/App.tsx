import { useCallback, useEffect, useState, type FormEvent, type ReactNode } from "react";
import { Icon } from "./Icons";

type Session = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  roles: string[];
};

type Product = {
  id: string;
  sku: string;
  name: string;
  description: string | null;
  unitPriceMinor: number;
};

type ApiError = {
  message?: string;
  fieldErrors?: Record<string, string>;
};

const api = async <T,>(path: string, token: string, init: RequestInit = {}): Promise<T> => {
  const response = await fetch(path, {
    ...init,
    headers: (() => {
      const headers = new Headers(init.headers);
      headers.set("Content-Type", "application/json");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      else headers.delete("Authorization");
      return headers;
    })(),
  });
  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as ApiError;
    throw new Error(body.message ?? `Request failed (${response.status})`);
  }
  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
};

function App() {
  const [session, setSession] = useState<Session | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isRegistering, setIsRegistering] = useState(false);
  const [productName, setProductName] = useState("");
  const [sku, setSku] = useState("");
  const [price, setPrice] = useState("");
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);

  const loadProducts = useCallback(async (token: string) => {
    const rows = await api<Product[]>("/api/v1/products", token);
    setProducts(rows);
  }, []);

  useEffect(() => {
    if (!session) return;
    void loadProducts(session.accessToken).catch((cause: unknown) => {
      setError(cause instanceof Error ? cause.message : "Could not load the catalog");
    });
  }, [loadProducts, session]);

  const submitAuth = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const endpoint = isRegistering ? "register" : "login";
      const next = await api<Session>(`/api/v1/auth/${endpoint}`, "", {
        method: "POST",
        headers: {},
        body: JSON.stringify({ email, password }),
      });
      setSession(next);
      setNotice(isRegistering ? "Supplier account created." : "Signed in successfully.");
      setPassword("");
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Authentication failed");
    } finally {
      setBusy(false);
    }
  };

  const refreshSession = async () => {
    if (!session) return;
    setBusy(true);
    setError("");
    try {
      const next = await api<Session>("/api/v1/auth/refresh", "", {
        method: "POST",
        headers: {},
        body: JSON.stringify({ refreshToken: session.refreshToken }),
      });
      setSession(next);
      setNotice("Session refreshed. The previous refresh token can no longer be used.");
    } catch (cause) {
      setSession(null);
      setError(cause instanceof Error ? cause.message : "Your session has expired");
    } finally {
      setBusy(false);
    }
  };

  const createProduct = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!session) return;
    setBusy(true);
    setError("");
    setNotice("");
    try {
      await api<Product>("/api/v1/products", session.accessToken, {
        method: "POST",
        body: JSON.stringify({
          sku,
          name: productName,
          description: "",
          unitPriceMinor: Math.round(Number(price) * 100),
        }),
      });
      setProductName("");
      setSku("");
      setPrice("");
      await loadProducts(session.accessToken);
      setNotice("Product added to the catalog.");
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not save the product");
    } finally {
      setBusy(false);
    }
  };

  const signOut = () => {
    setSession(null);
    setProducts([]);
    setNotice("");
    setError("");
  };

  return (
    <div className="min-h-screen bg-[#f4f7f6] text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-5 py-4 lg:px-8">
          <a className="flex items-center gap-3" href="/" aria-label="Northstar SCM home">
            <span className="grid size-10 place-items-center rounded-xl bg-teal-700 text-white">
              <Icon name="boxes" size={21} />
            </span>
            <span>
              <span className="block text-sm font-bold tracking-wide text-slate-950">NORTHSTAR</span>
              <span className="block text-xs text-slate-500">SUPPLY CHAIN OPERATIONS</span>
            </span>
          </a>
          {session ? (
            <div className="flex items-center gap-4">
              <span className="hidden rounded-full bg-teal-50 px-3 py-1.5 text-xs font-semibold text-teal-800 sm:block">
                {session.roles.join(" · ")}
              </span>
              <button className="icon-button" onClick={refreshSession} disabled={busy} title="Rotate refresh token">
                <Icon name="refresh" size={16} />
                <span className="hidden sm:inline">Refresh</span>
              </button>
              <button className="icon-button" onClick={signOut}>
                <Icon name="logout" size={16} />
                <span className="hidden sm:inline">Sign out</span>
              </button>
            </div>
          ) : (
            <span className="inline-flex items-center gap-2 text-sm text-slate-600">
              <Icon name="shield" size={17} className="text-teal-700" /> Secure workspace
            </span>
          )}
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-5 py-10 lg:px-8">
        {error && (
          <div role="alert" className="mb-6 flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
            <Icon name="alert" size={18} className="mt-0.5 shrink-0" />
            <span>{error}</span>
          </div>
        )}
        {notice && (
          <div role="status" className="mb-6 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">
            {notice}
          </div>
        )}

        {session ? (
          <section>
            <div className="mb-9 flex flex-wrap items-end justify-between gap-4">
              <div>
                <p className="eyebrow">OPERATIONS OVERVIEW</p>
                <h1 className="mt-2 text-3xl font-semibold tracking-tight text-slate-950">Supply chain, in sync.</h1>
                <p className="mt-2 max-w-xl text-sm leading-6 text-slate-600">
                  Review your product catalog and keep purchasing and warehouse teams aligned.
                </p>
              </div>
              <button className="secondary-button" onClick={() => void loadProducts(session.accessToken)} disabled={busy}>
                <Icon name="refresh" size={16} /> Refresh data
              </button>
            </div>

            <div className="mb-8 grid gap-4 md:grid-cols-3">
              <SummaryCard icon={<Icon name="boxes" size={18} />} label="Active products" value={products.length.toString()} tone="teal" />
              <SummaryCard icon={<Icon name="package" size={18} />} label="Catalog status" value="Connected" tone="blue" />
              <SummaryCard icon={<Icon name="truck" size={18} />} label="Fulfillment" value="Ready for orders" tone="amber" />
            </div>

            <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
              <section className="panel overflow-hidden">
                <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
                  <div>
                    <h2 className="font-semibold text-slate-950">Product catalog</h2>
                    <p className="mt-1 text-xs text-slate-500">Versioned supply service · live inventory catalog</p>
                  </div>
                  <span className="rounded-lg bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-600">{products.length} SKUs</span>
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[600px] text-left text-sm">
                    <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                      <tr>
                        <th className="px-5 py-3 font-medium">Product</th>
                        <th className="px-5 py-3 font-medium">SKU</th>
                        <th className="px-5 py-3 text-right font-medium">Unit price</th>
                        <th className="px-5 py-3 text-right font-medium">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {products.map((product) => (
                        <tr key={product.id} className="hover:bg-slate-50/80">
                          <td className="px-5 py-4">
                            <span className="font-medium text-slate-900">{product.name}</span>
                            {product.description && <span className="mt-1 block text-xs text-slate-500">{product.description}</span>}
                          </td>
                          <td className="px-5 py-4 font-mono text-xs text-slate-600">{product.sku}</td>
                          <td className="px-5 py-4 text-right font-medium text-slate-800">
                            {(product.unitPriceMinor / 100).toLocaleString(undefined, { style: "currency", currency: "USD" })}
                          </td>
                          <td className="px-5 py-4 text-right"><span className="status-pill">Active</span></td>
                        </tr>
                      ))}
                      {products.length === 0 && (
                        <tr><td className="px-5 py-12 text-center text-sm text-slate-500" colSpan={4}>No products yet. Add your first catalog item.</td></tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </section>

              <section className="panel h-fit">
                <div className="border-b border-slate-100 px-5 py-4">
                  <h2 className="font-semibold text-slate-950">Add a product</h2>
                  <p className="mt-1 text-xs text-slate-500">Requires Admin or Procurement Manager access</p>
                </div>
                <form className="space-y-4 p-5" onSubmit={createProduct}>
                  <label className="field-label">Product name
                    <input className="field-input" value={productName} onChange={(event) => setProductName(event.target.value)} maxLength={240} required />
                  </label>
                  <label className="field-label">SKU
                    <input className="field-input font-mono" value={sku} onChange={(event) => setSku(event.target.value)} maxLength={80} required />
                  </label>
                  <label className="field-label">Unit price (USD)
                    <input className="field-input" type="number" min="0.01" step="0.01" value={price} onChange={(event) => setPrice(event.target.value)} required />
                  </label>
                  <button className="primary-button w-full" type="submit" disabled={busy}>
                    Add catalog item <Icon name="arrow" size={16} />
                  </button>
                </form>
              </section>
            </div>
          </section>
        ) : (
          <div className="grid min-h-[72vh] items-center gap-12 py-8 lg:grid-cols-[1.05fr_0.95fr]">
            <section className="max-w-xl">
              <p className="eyebrow">ENTERPRISE SUPPLY CHAIN PLATFORM</p>
              <h1 className="mt-5 text-4xl font-semibold leading-tight tracking-tight text-slate-950 sm:text-5xl">
                Move every part of your supply chain <span className="text-teal-700">forward.</span>
              </h1>
              <p className="mt-5 max-w-lg text-base leading-7 text-slate-600">
                Connect suppliers, inventory, orders, and logistics in one secure operations workspace.
              </p>
              <div className="mt-8 flex flex-wrap gap-3 text-xs font-medium text-slate-600">
                <span className="feature-chip"><Icon name="shield" size={14} /> Role-based access</span>
                <span className="feature-chip"><Icon name="boxes" size={14} /> Live catalog</span>
                <span className="feature-chip"><Icon name="truck" size={14} /> Shipment tracking</span>
              </div>
            </section>

            <section className="panel mx-auto w-full max-w-md p-7 sm:p-8">
              <p className="eyebrow">NORTHSTAR SCM</p>
              <h2 className="mt-2 text-2xl font-semibold text-slate-950">{isRegistering ? "Create supplier access" : "Welcome back"}</h2>
              <p className="mt-2 text-sm text-slate-500">
                {isRegistering ? "New self-service accounts are assigned the Supplier role." : "Sign in to your supply chain workspace."}
              </p>
              <form className="mt-7 space-y-5" onSubmit={submitAuth}>
                <label className="field-label">Work email
                  <input className="field-input" type="email" autoComplete="username" value={email} onChange={(event) => setEmail(event.target.value)} maxLength={320} required />
                </label>
                <label className="field-label">Password
                  <input className="field-input" type="password" autoComplete={isRegistering ? "new-password" : "current-password"} value={password} onChange={(event) => setPassword(event.target.value)} minLength={12} maxLength={128} required />
                </label>
                <button className="primary-button w-full" type="submit" disabled={busy}>
                  {busy ? "Please wait…" : isRegistering ? "Create account" : "Sign in"}
                  {!busy && <Icon name="arrow" size={16} />}
                </button>
              </form>
              <p className="mt-6 text-center text-sm text-slate-500">
                {isRegistering ? "Already have access?" : "New supplier?"}{" "}
                <button className="font-semibold text-teal-800 hover:text-teal-950" onClick={() => { setIsRegistering(!isRegistering); setError(""); }}>
                  {isRegistering ? "Sign in" : "Create supplier account"}
                </button>
              </p>
              <p className="mt-6 border-t border-slate-100 pt-5 text-center text-xs leading-5 text-slate-400">
                Access tokens remain in memory for this browser session. Use your organization's managed identity provider in production.
              </p>
            </section>
          </div>
        )}
      </main>
    </div>
  );
}

function SummaryCard({ icon, label, value, tone }: { icon: ReactNode; label: string; value: string; tone: "teal" | "blue" | "amber" }) {
  const styles = {
    teal: "bg-teal-50 text-teal-700",
    blue: "bg-blue-50 text-blue-700",
    amber: "bg-amber-50 text-amber-700",
  }[tone];

  return (
    <div className="panel flex items-center gap-4 p-5">
      <span className={`grid size-11 place-items-center rounded-xl ${styles}`}>{icon}</span>
      <span><span className="block text-xs text-slate-500">{label}</span><span className="mt-1 block text-lg font-semibold text-slate-900">{value}</span></span>
    </div>
  );
}

export default App;
