# Budgether

Aplicativo de gerenciamento financeiro desenvolvido para a disciplina "Programação de Dispositivos Android 1" do curso "Especialização em Desenvolvimento de Sistemas para Dispositivos Móveis" do IFSP campus São Carlos.

[Protótipo navegável](https://www.figma.com/proto/GmJHHJ6vECRGKV80wFg7VV/Budgether-Material-Dark-Theme)

## Descrição

O trabalho aqui descrito constitui parte fundamental do conceito desta disciplina e consiste na implementação de uma aplicação móvel cujos usuários em potencial são pessoas que buscam manter controle da sua vida financeira pessoal. A ideia é bastante simples: o aplicativo deve manter uma organização de débitos e créditos na(s) conta(s) bancária(s) do usuário.

O usuário poderá cadastrar diferentes contas usando apenas uma descrição e um saldo inicial. Uma vez cadastradas as contas, o usuário poderá entrar com transações de crédito e débito que modificarão o saldo. Obviamente, crédito deverá somar ao saldo e débito deverá subtrair do saldo.

As transações deverão ser classificadas por tipo como: alimentação, saúde, transporte, moradia, educação, lazer, tarifas bancárias, luz, água, telefone, etc.

Ao dar entrada de uma transação o usuário entrará com uma breve descrição da mesma, selecionará a conta relativa à transação, entrará com o valor e selecionará o tipo de transação.

Para cada transação, o usuário deverá selecionar se essa é única ou uma transação que se repete. Caso se repita, o usuário poderá selecionar a periodicidade.

É primordial que o aplicativo mantenha um saldo atual para cada conta e um saldo atual total que é o somatório dos saldos atuais das contas cadastradas.

O aplicativo permitirá ao usuário gerar os seguintes extratos:
- Por conta (por período);
- Por natureza da transação (crédito ou débito);
- Por tipo de transação (alimentação, saúde, etc).

Funcionalidades desejáveis, mas não obrigatórias, são:
- Visualização gráfica dos extratos;
- Cadastro de transações de transferência entre contas;
- Agendamento de transações;
- Saldo previsto com base no histórico de gastos e transações agendadas;

A Google Play Store dispõe de vários aplicativos de funcionalidade parecidas que podem ser usados como base para o desenvolvimento do trabalho como: Mobils, Guiabolso, Minhas Finanças e Organizze.

São requisitos do projeto:
- Ter mais de uma tela;
- Conter a passagem de parâmetros entre as telas;
- Conter o retorno após o fechamento de uma Activity filha;
- Abranger diferente tipos de Activities;
- Usar adaptador sempre que necessário;
- Estruturar leiautes bem definidos (isso não quer dizer complexo)
- Projeto bem estruturado;
- Código bem estruturado, comentado e indentado;
- Seguir padrões de nomenclatura.
