import { formatRupiah } from "@/app/lib/format";

type Props = {
  balance: number;
  totalIncome: number;
  totalExpense: number;
};

export function BalanceSummary({ balance, totalIncome, totalExpense }: Props) {
  const flow = totalIncome + totalExpense;
  const incomePct = flow === 0 ? 0 : Math.round((totalIncome / flow) * 100);

  return (
    <section
      aria-labelledby="saldo-heading"
      className="mt-8 rounded-3xl border border-line bg-surface p-6 sm:p-8"
    >
      <h2 id="saldo-heading" className="text-sm font-medium text-muted">
        Saldo akhir
      </h2>
      <p
        className={`mt-1 text-4xl font-extrabold tracking-tight tabular-nums sm:text-5xl ${
          balance < 0 ? "text-expense" : "text-ink"
        }`}
      >
        {formatRupiah(balance)}
      </p>
      {balance < 0 && (
        <p className="mt-1 text-sm text-expense">Pengeluaranmu melebihi pemasukan.</p>
      )}

      {/* Bar perbandingan pemasukan vs pengeluaran */}
      <div
        className="mt-6 flex h-3 overflow-hidden rounded-full bg-line"
        role="img"
        aria-label={
          flow === 0
            ? "Belum ada arus kas"
            : `Pemasukan ${incomePct} persen, pengeluaran ${100 - incomePct} persen dari arus kas`
        }
      >
        {flow > 0 && (
          <>
            <div className="bg-income transition-[width] duration-500" style={{ width: `${incomePct}%` }} />
            <div className="flex-1 bg-expense" />
          </>
        )}
      </div>

      <dl className="mt-4 grid grid-cols-2 gap-4">
        <div>
          <dt className="flex items-center gap-2 text-sm text-muted">
            <span aria-hidden className="size-2.5 rounded-full bg-income" />
            Total pemasukan
          </dt>
          <dd className="mt-1 text-lg font-semibold tabular-nums text-income sm:text-xl">
            {formatRupiah(totalIncome)}
          </dd>
        </div>
        <div className="text-right">
          <dt className="flex items-center justify-end gap-2 text-sm text-muted">
            <span aria-hidden className="size-2.5 rounded-full bg-expense" />
            Total pengeluaran
          </dt>
          <dd className="mt-1 text-lg font-semibold tabular-nums text-expense sm:text-xl">
            {formatRupiah(totalExpense)}
          </dd>
        </div>
      </dl>
    </section>
  );
}
