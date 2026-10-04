import { NextResponse } from "next/server";
import { getAuthedClient, unauthorized } from "@/app/lib/api-auth";

export async function GET() {
  const { supabase, user } = await getAuthedClient();
  if (!user) return unauthorized();

  const { data, error } = await supabase
    .from("transactions")
    .select("type, amount, category, transaction_date")
    .eq("user_id", user.id);

  if (error) return NextResponse.json({ error: error.message }, { status: 500 });

  let totalIncome = 0, totalExpense = 0;
  const byMonth: Record<string, { income: number; expense: number }> = {};
  const byCategory: Record<string, number> = {};

  for (const t of data) {
    const amt = Number(t.amount);
    const month = String(t.transaction_date).slice(0, 7);
    byMonth[month] ??= { income: 0, expense: 0 };
    if (t.type === "income") {
      totalIncome += amt;
      byMonth[month].income += amt;
    } else {
      totalExpense += amt;
      byMonth[month].expense += amt;
      const c = t.category || "Lainnya";
      byCategory[c] = (byCategory[c] ?? 0) + amt;
    }
  }

  return NextResponse.json({
    totalIncome,
    totalExpense,
    balance: totalIncome - totalExpense,
    chart: {
      monthly: Object.entries(byMonth).sort().map(([month, v]) => ({ month, ...v })),
      expenseByCategory: Object.entries(byCategory).map(([category, total]) => ({ category, total })),
    },
  });
}