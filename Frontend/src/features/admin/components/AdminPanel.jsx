import { useEffect, useState } from "react";
import { api } from "../../../lib/api";

export default function AdminPanel({ me, onError }) {
    const [users, setUsers] = useState([]);

    async function loadUsers() {
        try {
            setUsers(await api("GET", "/users"));
        } catch (error) {
            onError(error.message);
        }
    }

    async function runAction(path, body) {
        try {
            await api("POST", path, body);
            onError("");
            await loadUsers();
        } catch (error) {
            onError(error.message);
        }
    }

    useEffect(() => {
        let cancelled = false;
        api("GET", "/users")
            .then((loadedUsers) => {
                if (!cancelled) setUsers(loadedUsers);
            })
            .catch((error) => {
                if (!cancelled) onError(error.message);
            });

        return () => {
            cancelled = true;
        };
    }, [onError]);

    return (
    <section>
      <h2>Użytkownicy ({users.length})</h2>
      <table>
        <thead>
          <tr>
            <th>E-mail</th>
            <th>Nazwa</th>
            <th>Rola</th>
            <th>Zablokowany</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>{user.email}</td>
              <td>{user.displayName}</td>
              <td>{user.role}</td>
              <td>{user.blocked ? "tak" : "nie"}</td>
              <td>
                {user.id !== me.id && (
                  <>
                    <button onClick={() => runAction("/users/block", { id: user.id, blocked: !user.blocked })}>
                      {user.blocked ? "Odblokuj" : "Zablokuj"}
                    </button>
                    <button onClick={() => runAction("/users/role", { id: user.id, role: user.role === "ADMIN" ? "USER" : "ADMIN" })}>
                      {user.role === "ADMIN" ? "Odbierz admina" : "Mianuj adminem"}
                    </button>
                    <button onClick={() => confirm(`Usunąć ${user.email}?`) && runAction("/users/delete", { id: user.id })}>
                      Usuń
                    </button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}