# Por Perto · 

App de uma tela só: mostra onde você está, encontra estabelecimentos ao redor e entrega o trajeto até o escolhido para o Google Maps.

A tela é só o mapa. Tocar num marcador abre o balão com o nome e o endereço do lugar; tocar no balão sai do app e cai no Google Maps com a rota montada.

| O que o app faz | Serviço do Google | Onde está no código |
|---|---|---|
| Mostra o mapa | Maps SDK for Android | `activity_main.xml` (`SupportMapFragment`) e `onMapReady()` |
| Descobre a posição do aparelho | Fused Location Provider (Google Play services, sem chave) | `ativarMinhaPosicao()` |
| Busca lugares por categoria | Places API (New), Nearby Search | `busca/BuscaLugares.java` |
| Abre o trajeto no Google Maps | Maps URLs (sem chave, sem cobrança) | `rota/AbrirNoMaps.java` e `abrirNoMaps()` |

Só a busca de lugares entra na cobrança. A localização do aparelho é do Google Play services, e o endereço que abre o Maps é uma URL pública.

## O que o app não faz

Não calcula rota. Ele monta um endereço da Maps URLs API e pede ao Android que abra:

```
https://www.google.com/maps/dir/?api=1
    &origin=-23.618500%2C-46.566000
    &destination=-23.615000%2C-46.570100
```

Quem desenha o caminho, soma o tempo e fala as instruções em voz alta é o Google Maps, que já está instalado no celular. É o mesmo padrão que o iFood e o Uber usam quando mandam você para a navegação.


## Preparar a chave para usar no Projeto

1. Entre em console.cloud.google.com e crie um projeto só para a disciplina (por exemplo `porperto`). Não reaproveite um projeto que tenha outras coisas.
2. Vincule uma conta de faturamento. O Maps SDK e o Places exigem, mesmo que o uso fique dentro da cota gratuita.
3. Em Faturamento > Orçamentos e alertas, crie um orçamento baixo com alerta por e-mail. É o seu detector de fumaça.
4. Em APIs e serviços > Biblioteca, ative **Maps SDK for Android** e **Places API (New)**. Atenção ao "(New)": a Places API antiga é outro produto e não funciona com este código. Não é preciso ativar nada para abrir o Maps.
5. Em Google Maps Platform > Chaves e credenciais, crie uma chave.
6. Na chave, em Restrições de API, marque só as duas APIs acima.
7. Em Restrições de aplicativo, deixe **Nenhuma**. Explicação logo abaixo.
8. Em APIs e serviços, abra a Places API (New), vá em Cotas e reduza o limite de requisições por dia. A cota diária impede que um laço infinito do programa gaste o mês inteiro numa tarde. 


### Por que não restringir a chave ao app Android

A restrição por app Android exige cadastrar o nome do pacote e o SHA-1 do certificado de cada computador que compila o projeto. Numa sala com 20 máquinas e alunos usando notebook próprio, isso não fecha.

### Quanto isso custa

Dentro do limite gratuito mensal do Google Maps Platform, nada. Os números abaixo são da tabela de preços em novembro de 2026; confira antes de cada semestre.

| Chamada | Categoria | Gratuito por mês |
|---|---|---|
| Mapa no Android (Maps SDK) | Essentials | ilimitado |
| Nearby Search com ID, nome, endereço e localização | Pro | 5.000 |
| Nearby Search pedindo nota ou horário de funcionamento | Enterprise | 1.000 |
| Abrir o trajeto no Maps (Maps URLs) | — | sem cobrança e sem chave |

O projeto foi escrito para ficar na linha barata: `BuscaLugares` pede só campos Pro.

## Para o aluno: rodar o projeto

1. Abra a pasta do projeto no Android Studio e espere o Gradle sincronizar.
2. Abra o arquivo `local.properties` na raiz do projeto. Ele já existe, com a linha `sdk.dir`.
3. Acrescente uma linha com a chave que o professor passou:
   ```
   MAPS_API_KEY=AIza...
   ```
4. Clique em **Sync Now**. Sem sincronizar, o `BuildConfig.MAPS_API_KEY` continua vazio.
5. Use um emulador com imagem **Google Play** ou **Google APIs**. A imagem "AOSP" pura não tem Google Play services, então o mapa não carrega e o botão Abrir no Maps não acha para onde ir.
6. Rode o app.

`local.properties` está no `.gitignore`. Se aparecer no `git status`, pare e peça ajuda antes de fazer commit.

### Colocar o emulador no campus

O emulador não tem GPS de verdade. Você diz a ele onde está:

1. Na barra lateral do emulador, clique nos três pontinhos (Extended Controls).
2. Abra **Location**.
3. Pesquise `Rua Santo André, 680, São Caetano do Sul` e clique em **Set location**.
4. Volte ao app e toque no botão de mira no canto do mapa.

Se `getCurrentLocation()` devolver `null` mesmo assim, abra o app Google Maps do emulador uma vez, espere ele achar a posição e volte.

A constante `CAMPUS` em `MainActivity` guarda uma coordenada aproximada do campus, usada só como câmera inicial. Se quiser precisão, clique com o botão direito no prédio no Google Maps, copie as coordenadas e troque os valores.

## Quando algo dá errado

Filtre o Logcat pela tag `PorPerto`. Quase todo erro de API chega com uma mensagem legível, e o endereço que vai para o Maps é impresso lá antes de abrir.
