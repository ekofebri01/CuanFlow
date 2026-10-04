"use client";

import { useEffect, useState } from "react";
import { BudgetIndicator } from "@/components/budget/BudgetIndicator";

export function BudgetSection() {
  const now = new Date();
  const [month] = useState(now.getMonth() + 1);
  const [year] = useState(now.getFullYear());
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    const handler = () => setRefreshKey((k) => k + 1);
    window.addEventListener("transaction:updated", handler);
    return () => window.removeEventListener("transaction:updated", handler);
  }, []);

  return (
    <section
      aria-labelledby="budget-heading"
      className="mt-6 rounded-3xl border border-line bg-surface p-6 sm:p-8"
    >
      <h2 id="budget-heading" className="text-lg font-bold">
        Status anggaran
      </h2>

      <BudgetIndicator month={month} year={year} refreshKey={refreshKey} />
    </section>
  );
}