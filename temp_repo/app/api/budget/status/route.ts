import { NextRequest, NextResponse } from "next/server";
import { createClient } from "@/utils/supabase/server";
import { calculateBudgetStatus } from "@/app/lib/budget-indicator";

export async function GET(request: NextRequest) {
  const supabase = await createClient();

  const { data: { user }, error: authError } = await supabase.auth.getUser();
  if (authError || !user) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const month = Number(request.nextUrl.searchParams.get("month"));
  const year = Number(request.nextUrl.searchParams.get("year"));
  if (!month || !year || month < 1 || month > 12) {
    return NextResponse.json(
      { error: "Parameter month (1-12) dan year wajib diisi" },
      { status: 400 },
    );
  }

  // Budget bulan ini
  const { data: budgetRow, error: budgetError } = await supabase
    .from("budgets")
    .select("amount")
    .eq("user_id", user.id)
    .eq("month", month)
    .eq("year", year)
    .maybeSingle();

  if (budgetError) {
    return NextResponse.json({ error: budgetError.message }, { status: 500 });
  }

  const budgetAmount = Number(budgetRow?.amount ?? 0);

  // Total pengeluaran bulan tersebut
  const startDate = `${year}-${String(month).padStart(2, "0")}-01`;
  const endMonth = month === 12 ? 1 : month + 1;
  const endYear = month === 12 ? year + 1 : year;
  const endDate = `${endYear}-${String(endMonth).padStart(2, "0")}-01`;

  const { data: expenseRows, error: expenseError } = await supabase
    .from("transactions")
    .select("amount")
    .eq("user_id", user.id)
    .eq("type", "expense")
    .gte("transaction_date", startDate)
    .lt("transaction_date", endDate);

  if (expenseError) {
    return NextResponse.json({ error: expenseError.message }, { status: 500 });
  }

  const totalExpense = (expenseRows ?? []).reduce((sum, r) => sum + Number(r.amount), 0);
  const indicator = calculateBudgetStatus(budgetAmount, totalExpense);

  return NextResponse.json({
    budgetAmount,
    totalExpense,
    remaining: budgetAmount - totalExpense,
    ...indicator,
  });
}