"use client";

import { useState, useTransition } from "react";
import { setPreferenceCookie } from "@/app/lib/actions/preferences";
import type { Theme } from "@/app/lib/types";

export function ThemeToggle({ initialTheme }: { initialTheme: Theme }) {
  const [theme, setTheme] = useState<Theme>(initialTheme);
  const [isPending, startTransition] = useTransition();
  const isDark = theme === "dark";

  function toggle() {
    const next: Theme = isDark ? "light" : "dark";
    setTheme(next);

    // Ubah tampilan seketika di browser...
    const root = document.documentElement;
    root.classList.toggle("dark", next === "dark");
    root.style.colorScheme = next;

    // ...lalu simpan ke cookie supaya tetap sama saat refresh.
    startTransition(async () => {
      const res = await setPreferenceCookie("theme", next);
      if (!res.ok) {
        setTheme(theme);
        root.classList.toggle("dark", isDark);
        root.style.colorScheme = theme;
      }
    });
  }

  return (
    <button
      type="button"
      onClick={toggle}
      aria-pressed={isDark}
      disabled={isPending}
      className="inline-flex items-center gap-2 rounded-full border border-line bg-surface px-3 py-2 text-sm font-medium hover:border-muted focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink disabled:opacity-70"
    >
      {isDark ? (
        <svg aria-hidden viewBox="0 0 24 24" className="size-4" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="12" cy="12" r="4" />
          <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
        </svg>
      ) : (
        <svg aria-hidden viewBox="0 0 24 24" className="size-4" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z" />
        </svg>
      )}
      {isDark ? "Mode terang" : "Mode gelap"}
    </button>
  );
}
