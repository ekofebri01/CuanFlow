"use client";

import { useEffect, useRef, useState } from "react";
import { formatRupiah } from "@/app/lib/format";
import { MONTH_NAMES, type BudgetSummary } from "@/app/lib/budget-types";

type Props = {
  month: number;
  year: number;
  initialAmount: number | null;
  onClose: () => void;
  onSaved: (summary: BudgetSummary) => void;
};

export function BudgetFormModal({ month, year, initialAmount, onClose, onSaved }: Props) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const [amount, setAmount] = useState(initialAmount ? String(initialAmount) : "");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  const isEdit = initialAmount !== null;
  const periodLabel = `${MONTH_NAMES[month - 1]} ${year}`;
  const preview = Number(amount);

  // Buka sebagai modal saat komponen muncul (Esc otomatis menutup)
  useEffect(() => {
    dialogRef.current?.showModal();
  }, []);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault(); // cegah submit biasa (reload) -> kirim lewat AJAX

    const value = Number(amount);
    if (!amount.trim() || !Number.isFinite(value) || value <= 0) {
      setError("Masukkan nominal lebih dari 0.");
      return;
    }

    setSaving(true);
    setError("");
    try {
      const res = await fetch("/api/budgets", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ month, year, amount: value }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error ?? "Gagal menyimpan budget.");
      onSaved(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal menyimpan budget.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <dialog
      ref={dialogRef}
      onClose={onClose}
      aria-labelledby="budget-form-title"
      className="m-auto w-[min(28rem,calc(100%-2rem))] rounded-3xl border border-line bg-surface p-6 text-ink backdrop:bg-black/50 sm:p-8"
    >
      <form onSubmit={handleSubmit} noValidate>
        <h2 id="budget-form-title" className="text-lg font-bold">
          {isEdit ? "Ubah" : "Atur"} budget {periodLabel}
        </h2>
        <p className="mt-1 text-sm text-muted">Batas maksimal pengeluaran untuk bulan ini.</p>

        <label htmlFor="budget-amount" className="mt-5 block text-sm font-medium">
          Nominal budget (Rp)
        </label>
        <input
          id="budget-amount"
          type="number"
          inputMode="numeric"
          min={1}
          step={1000}
          autoFocus
          value={amount}
          onChange={(e) => {
            setAmount(e.target.value);
            if (error) setError("");
          }}
          aria-invalid={error ? true : undefined}
          aria-describedby="budget-amount-help"
          placeholder="contoh: 1500000"
          className="mt-1 w-full rounded-xl border border-line bg-bg px-3 py-2.5 tabular-nums focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink"
        />
        <p id="budget-amount-help" className={`mt-1 text-sm ${error ? "text-expense" : "text-muted"}`}>
          {error || (preview > 0 ? formatRupiah(preview) : "Isi angka tanpa titik atau koma.")}
        </p>

        <div className="mt-6 flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="rounded-full px-4 py-2 text-sm font-medium text-muted hover:text-ink focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink"
          >
            Batal
          </button>
          <button
            type="submit"
            disabled={saving}
            className="rounded-full bg-ink px-4 py-2 text-sm font-semibold text-surface focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink disabled:opacity-60"
          >
            {saving ? "Menyimpan..." : "Simpan budget"}
          </button>
        </div>
      </form>
    </dialog>
  );
}