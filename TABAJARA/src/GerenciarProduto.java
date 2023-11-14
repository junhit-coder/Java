import javax.swing.JOptionPane;
import java.io.*;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class GerenciarProduto {
    private List<Produto> listaProdutos = new ArrayList<>();

    public void cadastrarProduto() {
        Produto produto = new Produto();

        produto.setNomeProduto(JOptionPane.showInputDialog("Digite o nome do produto:"));

        // Verifica se um produto com o mesmo nome já existe
        if (produtoJaCadastrado(produto.getNomeProduto())) {
            JOptionPane.showMessageDialog(null, "Um produto com o mesmo nome já está cadastrado. Por favor, escolha um nome diferente.", "Erro", JOptionPane.ERROR_MESSAGE);
            return; // Sai do método para evitar o cadastro duplicado
        }

        produto.setValorUnitario(Double.parseDouble(JOptionPane.showInputDialog("Digite o preço do produto (obs: usar . ao invés de ,):")));
        produto.setDescricao(JOptionPane.showInputDialog("Digite a descrição do produto:"));
        produto.setCodigo(Integer.parseInt(JOptionPane.showInputDialog("Digite o código de identificação do produto: ")));

        Object[] tiposProduto = {"Perecível", "Não Perecível"};
        Object tipoProdutoSelecionado = JOptionPane.showInputDialog(null, "Escolha o tipo do produto", "Tipo do Produto", JOptionPane.INFORMATION_MESSAGE, null, tiposProduto, tiposProduto[0]);

        if (tipoProdutoSelecionado != null) {
            if (tipoProdutoSelecionado.equals("Perecível")) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date dataValidade = dateFormat.parse(JOptionPane.showInputDialog("Digite a data de validade (Dia/Mês/Ano):"));
                    produto.setDataValidade(dataValidade);
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(null, "Formato de data inválido. Use o formato (Dia/Mês/Ano).", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }

            listaProdutos.add(produto);

            JOptionPane.showMessageDialog(null, "Produto cadastrado com sucesso.", "Cadastro Concluído", JOptionPane.INFORMATION_MESSAGE);

            salvarProdutosEmArquivo();
        }
    }

    private boolean produtoJaCadastrado(String nomeProduto) {
        for (Produto produto : listaProdutos) {
            if (produto.getNomeProduto().equalsIgnoreCase(nomeProduto)) {
                return true;
            }
        }
        return false;
    }


public void salvarProdutosEmArquivo() {
    String pastaBaseDados = "C:\\Visual Studio Code\\Java\\TABAJARA\\baseDados";
    File arquivo = new File(pastaBaseDados, "produtos.txt");

    try {
        if (!arquivo.getParentFile().exists()) {
            arquivo.getParentFile().mkdirs();
        }

        if (!arquivo.exists()) {
            arquivo.createNewFile();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo, false))) {
            for (Produto produto : listaProdutos) {
                // Escreva as informações do produto no arquivo
                writer.write("\n\nCódigo do produto: " + produto.getCodigo());
                writer.write("\nNOME DO PRODUTO: " + produto.getNomeProduto());
                writer.write("\nDescrição: " + produto.getDescricao());
                writer.write("\nValor unitário: R$" + produto.getValorUnitario());

                Date dataValidade = produto.getDataValidade();
                if (dataValidade != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    writer.write("\nData de validade: " + dateFormat.format(dataValidade));
                } else {
                    writer.write("\nData de validade: N/A"); // Se a data de validade não estiver definida
                }

                // Adiciona uma linha em branco para separar os dados dos produtos
                writer.write("\n");
            }

            JOptionPane.showMessageDialog(null, "Produtos salvos no arquivo 'produtos.txt' com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Erro ao salvar os produtos no arquivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
    //construtor vazio
    public GerenciarProduto() {

    }

    public void carregarProdutosDoArquivo() {
        String pastaBaseDados = "C:\\Visual Studio Code\\Java\\TABAJARA\\baseDados";
        File arquivo = new File(pastaBaseDados, "produtos.txt");

        if (arquivo.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    // Verifique se a linha contém dados de um produto
                    if (linha.startsWith("Código do produto: ")) {
                        int codigo = Integer.parseInt(linha.replace("Código do produto: ", ""));
                        String nomeProduto = reader.readLine().replace("NOME DO PRODUTO: ", "");
                        String descricao = reader.readLine().replace("Descrição: ", "");
                        double valorUnitario = Double.parseDouble(reader.readLine().replace("Valor unitário: R$", ""));
                        String dataValidadeStr = reader.readLine().replace("Data de validade: ", "");
                        Date dataValidade = null;
                        if (!dataValidadeStr.equals("N/A")) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            dataValidade = dateFormat.parse(dataValidadeStr);
                        }

                        // Criando um novo objeto Produto com os dados lidos e adicione-o à lista de produtos
                        Produto produto = new Produto(codigo,nomeProduto, descricao, valorUnitario, dataValidade);
                        listaProdutos.add(produto);
                    }
                }
            } catch (IOException | ParseException e) {
                JOptionPane.showMessageDialog(null, "Erro ao carregar produtos do arquivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void atualizarArquivoProdutos() {
        String pastaBaseDados = "C:\\Visual Studio Code\\Java\\TABAJARA\\baseDados";
        File arquivo = new File(pastaBaseDados, "produtos.txt");

        try {
            if (!arquivo.getParentFile().exists()) {
                arquivo.getParentFile().mkdirs();
            }

            if (!arquivo.exists()) {
                arquivo.createNewFile();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo, false))) {
                for (Produto produto : listaProdutos) {
                    // Escrevendo as informações do produto no arquivo, da mesma maneira que no método salvarProdutosEmArquivo
                    writer.write("\n\nCódigo do produto: " + produto.getCodigo());
                    writer.write("\nNOME DO PRODUTO: " + produto.getNomeProduto());
                    writer.write("\nDescrição: " + produto.getDescricao());
                    writer.write("\nValor unitário: R$" + produto.getValorUnitario());

                    Date dataValidade = produto.getDataValidade();
                    if (dataValidade != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        writer.write("\nData de validade: " + dateFormat.format(dataValidade));
                    } else {
                        writer.write("\nData de validade: N/A");
                    }

                    writer.write("\n");
                }

                JOptionPane.showMessageDialog(null, "Arquivo 'produtos.txt' atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar o arquivo 'produtos.txt': " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void listarProdutosEDeletarProdutoPeloIndice() {
        if (listaProdutos.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nenhum produto cadastrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        while (true) {
            // Listando todos os produtos cadastrados com um índice
            for (int i = 0; i < listaProdutos.size(); i++) {
                Produto produto = listaProdutos.get(i);
                System.out.println("[" + i + "] " + produto.getNomeProduto());
            }

            try {
                int escolha = Integer.parseInt(JOptionPane.showInputDialog("Digite o número do produto que deseja excluir (ou -1 para cancelar):"));

                if (escolha == -1) {
                    JOptionPane.showMessageDialog(null, "Operação cancelada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    break;
                }

                if (escolha >= 0 && escolha < listaProdutos.size()) {
                    Produto produtoExcluir = listaProdutos.get(escolha);
                    listaProdutos.remove(produtoExcluir);
                    salvarProdutosEmArquivo();
                    JOptionPane.showMessageDialog(null, "Produto com nome '" + produtoExcluir.getNomeProduto() + "' excluído com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    break; // Sai do loop depois de excluir o produto
                } else {
                    JOptionPane.showMessageDialog(null, "Índice inválido. Digite um número válido.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Entrada inválida. Digite um número válido.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public Produto encontrarProdutoPorNome(String nomeProduto) {
        for (Produto produto : listaProdutos) {
            if (produto.getNomeProduto().equalsIgnoreCase(nomeProduto)) {
                return produto;
            }
        }
        return null;
    }

    public List<Produto> getListaProdutos() {
        return listaProdutos;
    }

    public void limparOperacoesAnteriores() {
        listaProdutos.clear();
    }

    public void setListaProdutos(List<Produto> listaProdutosArquivo) {
    }

    // Método para obter uma representação em string da lista de produtos
    public String obterListaProdutosString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Lista de Produtos:\n");

        for (Produto produto : listaProdutos) {
            sb.append(produto.toString()).append("\n\n");
        }

        return sb.toString();
    }

    // Método para exibir a lista de produtos por meio do JOptionPane
    public void exibirListaProdutos() {
        String listaProdutosString = obterListaProdutosString();
        JOptionPane.showMessageDialog(null, listaProdutosString, "Lista de Produtos", JOptionPane.INFORMATION_MESSAGE);
    }
}