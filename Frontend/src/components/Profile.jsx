export default function Profile({ me, onLogout }) {
  return (
    <section>
        <h2>Moje dane</h2>
        <p><b>Email:</b> {me.email}</p>
        <p><b>Nazwa:</b> {me.displayName}</p>
        <p><b>Rola:</b> {me.role}</p>
        <p><b>Data utworzenia konta:</b> {new Date(me.createdAt).toLocaleDateString()}</p>
        {me.role === 'ADMIN' && <p>Ma uprawnienia administratora</p>}
        <button onClick={onLogout}>Wyloguj</button>
    </section>
  );
}