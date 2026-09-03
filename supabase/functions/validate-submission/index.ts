// Supabase Edge Function — validation d'une soumission (service role côté serveur)
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

Deno.serve(async (req) => {
  const { submissionId, comment } = await req.json();
  const supabase = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
  );
  const { error } = await supabase.from("submissions").update({
    status: "VALIDE",
    validated_at: new Date().toISOString(),
    admin_comment: comment,
  }).eq("id", submissionId);
  if (error) return new Response(JSON.stringify({ error: error.message }), { status: 400 });
  return new Response(JSON.stringify({ success: true }));
});
