import { redirect } from "next/navigation";
import { getDashboardSummary } from "@/app/lib/actions/dashboard";
import { getPreferenceCookie } from "@/app/lib/actions/preferences";
import { RecentTransactions } from "./_components/RecentTransactions";
import { ThemeToggle } from "./_components/ThemeToggle";
import { BudgetSection } from "./_components/BudgetSection";

import LogoutButton from '@/components/LogoutButton';
import { BudgetCard } from "./_components/BudgetCard";
import { currentPeriod } from "@/app/lib/budget-types";


import { BalanceSummaryLive } from "./_components/BalanceSummaryLive";


export const metadata = { title: "Dashboard | MoneyLover" };

export default async function DashboardPage() {
  const [theme, defaultView] = await Promise.all([
    getPreferenceCookie("theme"),
    getPreferenceCookie("default_view"),
  ]);

  const summary = await getDashboardSummary(defaultView);

  if (!summary) redirect("/login");

  return (
    <main className="mx-auto w-full max-w-3xl px-4 py-8 sm:px-6 lg:py-12">
      <header className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm text-muted">Halo,</p>
          <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">{summary.userName}</h1>
        </div>
        <ThemeToggle initialTheme={theme} />
        <LogoutButton />
      </header>

      <BalanceSummaryLive
        initial={{
          balance: summary.balance,
          totalIncome: summary.totalIncome,
          totalExpense: summary.totalExpense,
        }}
      />
      <BudgetCard initialMonth={currentPeriod().month} initialYear={currentPeriod().year} />

      <RecentTransactions transactions={summary.recentTransactions} activeView={defaultView} />

      <BudgetSection />
    </main>
  );
}