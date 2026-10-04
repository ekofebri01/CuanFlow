"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { formatRupiah } from "@/app/lib/format";
import { MONTH_NAMES, type BudgetSummary } from "@/app/lib/budget-types";
import { BudgetFormModal } from "./BudgetFormModal";

type Props = {
  initialMonth: number;
  initialYear: number;
};

type Status = "loading" | "ready" | "error";

/** Kabari komponen lain (mis. Budget Indicator Developer 3) bahwa data budget berubah. */
function broadcast(summary: BudgetSummary) {
  window.dispatchEvent(new CustomEvent<BudgetSummary>("budget:updated", { detail: summary }));
}

export function BudgetCard({ initialMonth, initialYear }: Props) {
  const [month, setMonth] = useState(initialMonth);
  const [year, setYear] = useState(initialYear);
  const [summary, setSummary] = useState<BudgetSummary | null>(null);
  const [status, setStatus] = useState<Status>("loading");
  const [errorMsg, setErrorMsg] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const abortRef = useRef<AbortController | null>(null);

  const years = [initialYear - 2, initialYear - 1, initialYear, initialYear + 1];
  const periodLabel = `${MONTH_NAMES[month - 1]} ${year}`;

  // AJAX: ambil budget bulan terpilih tanpa reload halaman
  const loadSummary = useCallback(async (m: number, y: number) => {
    abortRef.current?.abort(); // batalkan request lama kalau user cepat ganti bulan
    const controller = new AbortController();
    abortRef.current = controller;
    setStatus("loading");

    try {
      const res = await fetch(`/api/budgets?month=${m}&year=${y}`, {
        signal: controller.signal,
        cache: "no-store",
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error ?? "Gagal memuat budget.");
      setSummary(data);
      setStatus("ready");
      broadcast(data);
    } catch (err) {
      if (err instanceof DOMException && err.name === "AbortError") return;
      setErrorMsg(err instanceof Error ? err.message : "Gagal memuat budget.");
      setStatus("error");
    }
  }, []);

  useEffect(() => {
    loadSummary(month, year);
    return () => abortRef.current?.abort();
  }, [month, year, loadSummary]);

  // Hitung ulang saat Developer 1 memberi sinyal ada transaksi berubah
  useEffect(() => {
    const refresh = () => loadSummary(month, year);
    window.addEventListener("transactions:changed", refresh);
    return () => window.removeEventListener("transactions:changed", refresh);
  }, [month, year, loadSummary]);

  function handleSaved(data: BudgetSummary) {
    setSummary(data);
    setStatus("ready");
    setModalOpen(false);
    broadcast(data);
  }

  const hasBudget = summary !== null && summary.budget !== null;
  const isOver = hasBudget && (summary.remaining ?? 0) < 0;

  return (
    <section
      aria-labelledby="budget-heading"
      className="mt-6 rounded-3xl border border-line bg-surface p-6 sm:p-8"
    >
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 id="budget-heading" className="text-lg font-bold">
          Budget bulanan
        </h2>

        {/* Monthly Budget Selector */}
        <div className="flex gap-2">
          <label htmlFor="budget-month" className="sr-only">
            Bulan
          </label>
          <select
            id="budget-month"
            value={month}
            onChange={(e) => setMonth(Number(e.target.value))}
            className="rounded-full border border-line bg-surface px-3 py-1.5 text-sm focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink"
          >
            {MONTH_NAMES.map((name, i) => (
              <option key={name} value={i + 1}>
                {name}
              </option>
            ))}
          </select>

          <label htmlFor="budget-year" className="sr-only">
            Tahun
          </label>
          <select
            id="budget-year"
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
            className="rounded-full border border-line bg-surface px-3 py-1.5 text-sm focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink"
          >
            {years.map((y) => (
              <option key={y} value={y}>
                {y}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div aria-live="polite" aria-busy={status === "loading"} className="mt-5">
        {status === "error" ? (
          <div className="rounded-2xl border border-expense/40 px-4 py-6 text-center">
            <p className="text-expense">{errorMsg}</p>
            <button
              type="button"
              onClick={() => loadSummary(month, year)}
              className="mt-3 text-sm font-semibold underline underline-offset-4"
            >
              Coba lagi
            </button>
          </div>
        ) : summary === null ? (
          <div className="h-28 animate-pulse rounded-2xl bg-line/60" />
        ) : (
          <div className={`transition-opacity ${status === "loading" ? "opacity-50" : ""}`}>
            {hasBudget ? (
              <dl className="grid gap-4 sm:grid-cols-3">
                <div>
                  <dt className="text-sm text-muted">Limit anggaran</dt>
                  <dd className="mt-1 text-xl font-bold tabular-nums">{formatRupiah(summary.budget!)}</dd>
                </div>
                <div>
                  <dt className="text-sm text-muted">Total pengeluaran</dt>
                  <dd className="mt-1 text-xl font-bold tabular-nums text-expense">
                    {formatRupiah(summary.totalExpense)}
                  </dd>
                </div>
                <div>
                  <dt className="text-sm text-muted">{isOver ? "Melebihi anggaran" : "Sisa anggaran"}</dt>
                  <dd
                    className={`mt-1 text-xl font-bold tabular-nums ${isOver ? "text-expense" : "text-income"}`}
                  >
                    {formatRupiah(Math.abs(summary.remaining ?? 0))}
                  </dd>
                </div>
              </dl>
            ) : (
              <div className="rounded-2xl border border-dashed border-line px-4 py-6 text-center">
                <p className="font-medium">Belum ada budget untuk {periodLabel}.</p>
                <p className="mt-1 text-sm text-muted">
                  Pengeluaran bulan ini: {formatRupiah(summary.totalExpense)}
                </p>
              </div>
            )}
          </div>
        )}
      </div>

      {status !== "error" && summary !== null && (
        <button
          type="button"
          onClick={() => setModalOpen(true)}
          className="mt-5 rounded-full bg-ink px-4 py-2 text-sm font-semibold text-surface focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink"
        >
          {hasBudget ? "Ubah budget" : "Atur budget"}
        </button>
      )}

      {modalOpen && (
        <BudgetFormModal
          month={month}
          year={year}
          initialAmount={summary?.budget ?? null}
          onClose={() => setModalOpen(false)}
          onSaved={handleSaved}
        />
      )}
    </section>
  );
}