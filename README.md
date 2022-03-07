# News Portal

Aplicativo de notícias, onde o usuário pode visualizar as notícias, favoritar notícias e compartilhar notícias.

A idéia principal do aplicativo é um portal de notícias Offline First, que funcione online e offline.
Ao logar pela primeira vez, o aplicativo irá baixar as notícias de uma API e armazenará no banco de dados local,
e carregará as notícias do banco de dados para mostrar para o usuário, dessa forma o usuário poderá ter acesso
as notícias mesmo que depois ele fique sem conexão a internet.
O aplicativo irá fazer consultas a API periodicamente de 30 em 30 segundos e quando houve notícias novas
irá atualizar a banco de dados local, que através do livedata automaticamente irá atualizar a UI.

## O aplicativo contém as seguintes telas:

* Tela de Cadastro;
* Tela de Login;
* Tela Home;
* Tela de busca. 

## Tecnologias usadas:

 - Desenvolvido em Kotlin;
 - Retrofit;
 - Dagger Hilt para injeção de dependências;
 - Arquitetura MVVM;
 - ROOM Database;
 - SharedPreferences;
 - LiveData para atualização na UI conforme são inseridos dados no banco local;
 - Coroutines;
 - Glide para mostrar as imagens;
 - Material design;
 - Técnica de Debounce para pesquisa;
 - WebView para visualizar as notícias.
 
