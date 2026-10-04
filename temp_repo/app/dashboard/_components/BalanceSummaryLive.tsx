"use client";

import { useDashboardSummary } from "@/app/lib/use-transactions";
import { BalanceSummary } from "./BalanceSummary";

type Props = {
  initial: { balance: number; totalIncome: number; totalExpense: number };
};

export function BalanceSummaryLive({ initial }: Props) {
  const { data } = useDashboardSummary();

  return (
    <BalanceSummary
      balance={data?.balance ?? initial.balance}
      totalIncome={data?.totalIncome ?? initial.totalIncome}
      totalExpense={data?.totalExpense ?? initial.totalExpense}
    />
  );
}