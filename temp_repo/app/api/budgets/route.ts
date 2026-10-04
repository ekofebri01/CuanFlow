import { NextResponse, type NextRequest } from "next/server";
import { createClient } from "@/utils/supabase/server";
import { getBudgetSummary, parseMonthYear } from "@/app/lib/budget";

const MAX_AMOUNT = 999_999_999_999;

async function getSessionUser() {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  return { supabase, user };
}

const unauthorized = () =>
  NextResponse.json({ error: "Sesi berakhir, silakan login kembali." }, { status: 401 });

/**
 * GET /api/budgets?month=9&year=2026
 * Fitur 2 & 4: budget bulan terpilih + total pengeluaran + sisa anggaran.
 */
export async function GET(request: NextRequest) {
  const { supabase, user } = await getSessionUser();
  if (!user) return unauthorized();

  const params = request.nextUrl.searchParams;
  const period = parseMonthYear(params.get("month"), params.get("year"));
  if (!period) {
    return NextResponse.json({ error: "Parameter bulan atau tahun tidak valid." }, { status: 400 });
  }

  try {
    const summary = await getBudgetSummary(supabase, user.id, period.month, period.year);
    return NextResponse.json(summary);
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Gagal memuat budget." }, { status: 500 });
  }
}

/**
 * PUT /api/budgets   body: { month, year, amount }
 * Fitur 1: set budget baru, atau ubah jika bulan itu sudah punya budget (upsert).
 */
export async function PUT(request: NextRequest) {
  const { supabase, user } = await getSessionUser();
  if (!user) return unauthorized();

  let body: Record<string, unknown>;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ error: "Format data tidak valid." }, { status: 400 });
  }

  const period = parseMonthYear(body?.month, body?.year);
  const amount = Number(body?.amount);

  if (!period) {
    return NextResponse.json({ error: "Bulan atau tahun tidak valid." }, { status: 400 });
  }
  if (!Number.isFinite(amount) || amount <= 0 || amount > MAX_AMOUNT) {
    return NextResponse.json({ error: "Nominal budget harus lebih dari 0." }, { status: 400 });
  }

  // user_id selalu diambil dari sesi, BUKAN dari body, jadi tidak bisa dipalsukan.
  const { error } = await supabase.from("budgets").upsert(
    {
      user_id: user.id,
      month: period.month,
      year: period.year,
      amount: Math.round(amount * 100) / 100,
      updated_at: new Date().toISOString(),
    },
    { onConflict: "user_id,month,year" },
  );

  if (error) {
    console.error(error);
    return NextResponse.json({ error: "Gagal menyimpan budget." }, { status: 500 });
  }

  try {
    const summary = await getBudgetSummary(supabase, user.id, period.month, period.year);
    return NextResponse.json(summary);
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Budget tersimpan, tapi ringkasan gagal dimuat." }, { status: 500 });
  }
}