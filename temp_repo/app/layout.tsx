import type { Metadata } from "next";
import { Plus_Jakarta_Sans } from "next/font/google";
import { getPreferenceCookie } from "@/app/lib/actions/preferences";
import "./globals.css";

const jakarta = Plus_Jakarta_Sans({
  subsets: ["latin"],
  variable: "--font-jakarta",
  display: "swap",
});

export const metadata: Metadata = {
  title: "MoneyLover",
  description: "Catat pemasukan dan pengeluaran harianmu.",
};

export default async function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  // Tema dibaca dari cookie di server => class "dark" sudah ada di HTML awal,
  // jadi tidak ada kedipan terang->gelap saat refresh (NFR-02.2).
  const theme = await getPreferenceCookie("theme");

  return (
    <html
      lang="id"
      className={`${jakarta.variable} ${theme === "dark" ? "dark" : ""}`}
      style={{ colorScheme: theme }}
    >
      <body className="min-h-dvh bg-bg font-sans text-ink antialiased">{children}</body>
    </html>
  );
}
