# 📱 App Lista de Tarefas (Android + Kotlin)

> Aplicativo nativo em Android para gerenciamento completo de tarefas e compromissos diários, construído com interface unificada em Dark Theme, persistência local em JSON, arquitetura em camadas e manipulação dinâmica de listas.

---

## 🎯 Objetivo do Projeto
O projeto foi desenvolvido para atender aos requisitos da disciplina de **Desenvolvimento de Aplicativos Móveis**, integrando componentes visuais do Android SDK (`RecyclerView`, `CardView`, `TextClock`) e boas práticas de engenharia de software (CRUD completo, separação de responsabilidades, validações de entrada e tratamento de exceções).

---

## 🚀 Funcionalidades (CRUD Completo)

- **[C] Create (Cadastro):**
    - Validação de entrada: título obrigatório (3 a 50 caracteres) e descrição de até 200 caracteres.
    - Permite a seleção de datas e horários por componentes nativos do Android (`DatePickerDialog` e `TimePickerDialog`), reduzindo erros de entrada manual.
    - Classificação por prioridade (*Alta*, *Média*, *Baixa*) com cores dinâmicas.
    - Inserção de novos itens no topo da lista (`index 0`).

- **[R] Read (Listagem & Filtros):**
    - Listagem utilizando `RecyclerView` e `CardView`, aproveitando o mecanismo de reutilização de itens do `RecyclerView`.
    - Relógio digital (`TextClock`) e data em tempo real no cabeçalho.
    - Contador de pendências ativas versus tarefas concluídas.
    - Filtros rápidos por estado: **[Todas]**, **[Pendentes]** e **[Concluídas]**.
    - Feedback visual automático para lista vazia.

- **[U] Update (Edição):**
    - Botão contextual no card para carregar os dados no formulário, com opção de salvar alterações ou cancelar a edição.

- **[D] Delete (Exclusão Segura):**
    - Separação conceitual entre **Conclusão** (marcar via `CheckBox` com efeito tachado) e **Exclusão** permanente.
    - Diálogo de confirmação com `AlertDialog` para prevenir exclusões acidentais.

---

## 🏗️ Arquitetura e Organização do Código

O projeto segue o princípio de responsabilidade única (SRP), dividindo as camadas da aplicação: