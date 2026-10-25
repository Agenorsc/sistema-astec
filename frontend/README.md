# Astec — Frontend (React + Vite)

Interface web do Sistema Astec (gestão de assistência técnica), feita para consumir a API Spring Boot do projeto `sistema-astec`.

## Como rodar no VS Code

Pré-requisito: Node.js 18+ (`node -v`).

```bash
npm install
npm run dev        # http://localhost:5173
```

Dois modos (arquivo `.env`, copie de `.env.example`):

| `VITE_USE_MOCK` | O que acontece |
|---|---|
| `false` (padrão) | Usa a API real. Suba o backend antes: `./mvnw spring-boot:run` (porta 8080). O Vite faz proxy de `/api` → `localhost:8080`, então não há problema de CORS. |
| `true` | Usa dados falsos em memória (`src/api/mock.js`). Dá para programar a interface sem o backend. Reinicie o `npm run dev` ao mudar o `.env`. |

Logins de teste (criados pelo `DataInitializer` do backend): ver botões na tela de login.

## Estrutura

```
src/
  api/index.js        todas as chamadas HTTP (um método por endpoint)
  api/mock.js         backend falso com o mesmo formato de JSON
  auth/AuthContext    usuário logado + login/logout
  components/         Layout (menu lateral), Badges, State (loading/erro)
  pages/              Login, Dashboard, Tickets, TicketNew, TicketDetail, Equipamentos, Usuarios
  utils/              rótulos dos enums, formatação, hook useAsync
  styles.css          estilos (variáveis CSS, responsivo, modo escuro)
```

Para criar uma tela: crie `src/pages/MinhaTela.jsx`, registre a rota em `src/App.jsx` e (se precisar) um link em `components/Layout.jsx`.

## Requisitos do projeto atendidos

RF01 abrir chamado · RF03 atribuir técnico · RF04 status · RF07 painel do cliente · RF08 painel técnico/admin · RF09 e RF10 exibição de laudo e avaliação · RNF03 acesso por perfil · RNF06 layout responsivo.

## Limitações atuais do backend (o front já se adapta)

- **Sem login/JWT**: o login busca `/api/usuarios` e confere e-mail/senha no navegador. Isso é só para desenvolvimento (a API devolve a senha em texto). Troque `login()` em `AuthContext.jsx` por um `POST /api/auth/login` quando existir.
- **Sem endpoints** para mensagens (RF05), anexos (RF02), laudo/peças (RF09, criação), avaliação (RF10, criação) e reabertura (RF11). Quando criar os controllers, adicione os métodos em `src/api/index.js` e as seções em `TicketDetail.jsx`.
- O front **exige** duas correções feitas no backend (já aplicadas na pasta `sistema-astec-main` deste pacote): `@JsonIgnore` em `LaudoTecnico.ticket` e `Avaliacao.ticket` (evita recursão infinita no JSON) e o endpoint `PATCH /api/tickets/{id}/tecnico?tecnicoId=` (atribuir técnico).
