export default function Loading() {
  return (
    <main className="mx-auto w-full max-w-3xl px-4 py-8 sm:px-6 lg:py-12" aria-busy="true">
      <div className="h-8 w-40 animate-pulse rounded bg-line" />
      <div className="mt-8 h-52 animate-pulse rounded-2xl bg-line/60" />
      <div className="mt-6 h-72 animate-pulse rounded-2xl bg-line/60" />
      <span className="sr-only">Memuat dashboard</span>
    </main>
  );
}
