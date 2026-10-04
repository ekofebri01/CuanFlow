// app/api/transactions/route.ts
import { NextRequest, NextResponse } from "next/server";
import { getAuthedClient, unauthorized } from "@/app/lib/api-auth";
import { filterSchema, transactionSchema } from "@/app/lib/validation";

export async function GET(req: NextRequest) {
  const { supabase, user } = await getAuthedClient();
  if (!user) return unauthorized();

  const parsed = filterSchema.safeParse(
    Object.fromEntries(req.nextUrl.searchParams)
  );
  if (!parsed.success)
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });

  const { from, to, category, type, q, page, limit } = parsed.data;
  const start = (page - 1) * limit;

  let query = supabase
    .from("transactions")
    .select("*", { count: "exact" })
    .eq("user_id", user.id) // lapis kedua di atas RLS
    .order("date", { ascending: false })
    .range(start, start + limit - 1);

  if (from) query = query.gte("date", from);
  if (to) query = query.lte("date", to);
  if (category) query = query.eq("category", category);
  if (type) query = query.eq("type", type);
  if (q) query = query.ilike("description", `%${q.replace(/[%_]/g, "")}%`);

  const { data, count, error } = await query;
  if (error) return NextResponse.json({ error: error.message }, { status: 500 });

  return NextResponse.json({
    data,
    meta: { page, limit, total: count ?? 0, totalPages: Math.ceil((count ?? 0) / limit) },
  });
}

export async function POST(req: NextRequest) {
  const { supabase, user } = await getAuthedClient();
  if (!user) return unauthorized();

  const parsed = transactionSchema.safeParse(await req.json());
  if (!parsed.success)
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 })
  // user_id SELALU dari session, abaikan kalau client mengirimnya
  const { data, error } = await supabase
    .from("transactions")
    .insert({ ...parsed.data, user_id: user.id })
    .select()
    .single();

  if (error) return NextResponse.json({ error: error.message }, { status: 500 });
  return NextResponse.json({ data }, { status: 201 });
}