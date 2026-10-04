import Link from "next/link";
import type { DefaultView, Transaction } from "@/app/lib/types";
import { formatRupiah, formatTanggal } from "@/app/lib/format";
import { RecentFilterTabs } from "./RecentFilterTabs";

type Props = {
  transactions: Transaction[];
  activeView: DefaultView;
};

const EMPTY_TEXT: Record<DefaultView, string> = {
  all: "Belum ada transaksi.",
  income: "Belum ada pemasukan yang tercatat.",
  expense: "Belum ada pengeluaran yang tercatat.",
};

export function RecentTransactions({ transactions, activeView }: Props) {
  return (
    <section aria-labelledby="recent-heading" className="mt-6 rounded-3xl border border-line bg-surface p-6 sm:p-8">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 id="recent-heading" className="text-lg font-bold">
          5 transaksi terbaru
        </h2>
        <RecentFilterTabs active={activeView} />
      </div>

      {transactions.length === 0 ? (
        <div className="mt-6 rounded-2xl border border-dashed border-line px-4 py-10 text-center">
          <p className="text-muted">{EMPTY_TEXT[activeView]}</p>
          <Link
            href="/transactions"
            className="mt-3 inline-block rounded-full bg-ink px-4 py-2 text-sm font-semibold text-surface focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink"
          >
            Tambah transaksi
          </Link>
        </div>
      ) : (
        <ul className="mt-4 divide-y divide-line">
          {transactions.map((t) => {
            const isIncome = t.type === "income";
            return (
              <li key={t.id} className="flex items-center justify-between gap-4 py-3">
                <div className="min-w-0">
                  <p className="truncate font-medium">{t.category}</p>
                  <p className="truncate text-sm text-muted">
                  {formatTanggal(t.transaction_date)}
                  {t.transaction_note ? `, ${t.transaction_note}` : ""}
                </p>
                </div>
                <p
                  className={`shrink-0 font-semibold tabular-nums ${isIncome ? "text-income" : "text-expense"}`}
                >
                  <span className="sr-only">{isIncome ? "Pemasukan" : "Pengeluaran"} </span>
                  {isIncome ? "+" : "−"}
                  {formatRupiah(t.amount)}
                </p>
              </li>
            );
          })}
        </ul>
      )}

      <Link
        href="/transactions"
        className="mt-4 inline-block text-sm font-semibold underline underline-offset-4 hover:text-muted focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink"
      >
        Lihat semua transaksi
      </Link>
    </section>
  );
}
