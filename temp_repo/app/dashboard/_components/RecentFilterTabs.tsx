"use client";

import { useOptimistic, useTransition } from "react";
import { setPreferenceCookie } from "@/app/lib/actions/preferences";
import type { DefaultView } from "@/app/lib/types";

const OPTIONS: { value: DefaultView; label: string }[] = [
  { value: "all", label: "Semua" },
  { value: "income", label: "Pemasukan" },
  { value: "expense", label: "Pengeluaran" },
];

export function RecentFilterTabs({ active }: { active: DefaultView }) {
  const [isPending, startTransition] = useTransition();
  const [current, setCurrent] = useOptimistic(active);

  function select(value: DefaultView) {
    if (value === current) return;
    startTransition(async () => {
      setCurrent(value); // UI langsung berubah
      await setPreferenceCookie("default_view", value); // simpan cookie + render ulang di server
    });
  }

  return (
    <div
      role="group"
      aria-label="Tampilkan jenis transaksi"
      aria-busy={isPending}
      className="inline-flex rounded-full border border-line p-1 text-sm"
    >
      {OPTIONS.map((opt) => {
        const selected = current === opt.value;
        return (
          <button
            key={opt.value}
            type="button"
            aria-pressed={selected}
            onClick={() => select(opt.value)}
            className={`rounded-full px-3 py-1.5 font-medium transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink ${
              selected ? "bg-ink text-surface" : "text-muted hover:text-ink"
            }`}
          >
            {opt.label}
          </button>
        );
      })}
    </div>
  );
}
