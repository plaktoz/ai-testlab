export const USERS = [
  {
    id: 1,
    username: "alice",
    password: "password1",
    name: "Alice Johnson",
    email: "alice@example.com",
  },
  {
    id: 2,
    username: "bob",
    password: "h",
    name: "Bob Smith",
    email: "bob@example.com",
  },
];

export const TASKS = [
  { id: 1, userId: 1, title: "Buy groceries",     done: false },
  { id: 2, userId: 1, title: "Read a book",        done: true  },
  { id: 3, userId: 1, title: "Exercise",           done: false },
  { id: 4, userId: 2, title: "Fix the sink",       done: false },
  { id: 5, userId: 2, title: "Call the dentist",   done: true  },
  { id: 6, userId: 2, title: "Write a blog post",  done: false },
];
