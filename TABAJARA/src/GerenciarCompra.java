import javax.swing.JOptionPane;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GerenciarCompra {
    private GerenciarCliente gerenciarCliente;
    private GerenciarProduto gerenciarProduto;
    private List<Compra> listaCompras;

    public GerenciarCompra(GerenciarCliente gerenciarCliente, GerenciarProduto gerenciarProduto) {
        this.gerenciarCliente = gerenciarCliente;
        this.gerenciarProduto = gerenciarProduto;
        this.listaCompras = new ArrayList<>();
    }

    public void exibirListas() {
        gerenciarCliente.exibirListaClientes();
        gerenciarProduto.exibirListaProdutos();
    }

    public void realizarCompra() {
        // Obtendo a lista de produtos e clientes
        List<Produto> listaProdutos = gerenciarProduto.getListaProdutos();
        List<Cliente> listaClientes = gerenciarCliente.getListaClientes();

        // Se não houver produtos ou clientes, não é possível realizar uma compra
        if (listaProdutos.isEmpty() || listaClientes.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Não há produtos ou clientes cadastrados para realizar a compra.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Obtendo o cliente para a compra
        Cliente cliente = obterClienteParaCompra(listaClientes);
        if (cliente == null) {
            JOptionPane.showMessageDialog(null, "Operação cancelada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Obtendo a lista de itens para a compra
        List<ItemCompra> itensCompra = obterItensParaCompra(listaProdutos);

        // Se não houver itens selecionados, cancela a compra
        if (itensCompra.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Operação cancelada. Nenhum item selecionado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Obtendo a data da compra
        Date dataCompra = obterDataCompra();

        // Calculando o valor total da compra
        float valorTotal = calcularValorTotalCompra(itensCompra);

        // Criando um objeto Compra
        Compra compra = new Compra();
        compra.setIdentificador(gerarIdentificadorCompra());
        compra.setDataCompra(dataCompra);
        compra.setItensCompra(itensCompra);
        compra.setCpf(cliente instanceof PessoaFisica ? (PessoaFisica) cliente : null);
        compra.setCnpj(cliente instanceof PessoaJuridica ? (PessoaJuridica) cliente : null);
        compra.setValorTotal(valorTotal);

        // Exibir detalhes da compra e obter valor pago
        float valorPago = exibirDetalhesCompraEObterValorPago(compra);

        // Definir valores pagos e restantes
        compra.setTotalPago(valorPago);
        float faltaPagar = compra.getValorTotal() - valorPago;
        compra.setFaltaPagar(faltaPagar);

        // Adicionando a compra à lista de compras
        listaCompras.add(compra);

        // Salvando a compra no arquivo compras.txt
        salvarCompraEmArquivo(compra);

        // Exibir recibo
        exibirRecibo(compra, valorPago, faltaPagar);

        JOptionPane.showMessageDialog(null, "Compra realizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private float exibirDetalhesCompraEObterValorPago(Compra compra) {
        float valorTotal = compra.getValorTotal();

        StringBuilder detalhesCompra = new StringBuilder("Detalhes da Compra:\n");
        detalhesCompra.append("Cliente: ").append(compra.getCpf() != null ? compra.getCpf().getNome() : compra.getCnpj().getNome()).append("\n");
        detalhesCompra.append("Valor Total da Compra: R$ ").append(String.format("%.2f", valorTotal)).append("\n");

        // Exibir itens comprados
        detalhesCompra.append("Itens Comprados:\n");
        for (ItemCompra item : compra.getItensCompra()) {
            detalhesCompra.append("  - ").append(item.getQuantidade()).append("x ").append(item.getProduto().getNomeProduto())
                    .append(" - R$ ").append(String.format("%.2f", item.getProduto().getValorUnitario())).append("\n");
        }

        // Obter valor pago
        try {
            String valorPagoStr = JOptionPane.showInputDialog(detalhesCompra + "\nDigite o valor que deseja pagar (obs: insira com ponto ao invés da vírgula):");

            // Verificar se o usuário cancelou a entrada
            if (valorPagoStr == null) {
                return 0;
            }

            float valorPago = Float.parseFloat(valorPagoStr);

            // Verificar se o valor pago é válido
            while (valorPago < 0 || valorPago > valorTotal) {
                valorPagoStr = JOptionPane.showInputDialog("Valor excedeu o valor total da compra. Digite novamente o valor que deseja pagar:");

                // Verificar se o usuário cancelou a entrada
                if (valorPagoStr == null) {
                    return 0;
                }

                valorPago = Float.parseFloat(valorPagoStr);
            }

            return valorPago;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Entrada inválida. Operação cancelada.", "Erro", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }

    private void exibirRecibo(Compra compra, float valorPago, float faltaPagar) {
        StringBuilder recibo = new StringBuilder("==== RECIBO ====\n");
        recibo.append("Identificador: ").append(compra.getIdentificador()).append("\n");
        recibo.append("Data da Compra: ").append(new SimpleDateFormat("dd/MM/yyyy").format(compra.getDataCompra())).append("\n");

        // Exibindo CPF ou CNPJ do cliente
        if (compra.getCpf() != null) {
            recibo.append("Cliente (CPF): ").append(compra.getCpf().getNome()).append(" - ").append(((PessoaFisica) compra.getCpf()).getCpf()).append("\n");
        } else if (compra.getCnpj() != null) {
            recibo.append("Cliente (CNPJ): ").append(compra.getCnpj().getNome()).append(" - ").append(((PessoaJuridica) compra.getCnpj()).getCnpj()).append("\n");
        }

        recibo.append("Valor Total: R$ ").append(String.format("%.2f", compra.getValorTotal())).append("\n");
        recibo.append("Total Pago: R$ ").append(String.format("%.2f", valorPago)).append("\n");
        recibo.append("Falta Pagar: R$ ").append(String.format("%.2f", faltaPagar)).append("\n");

        JOptionPane.showMessageDialog(null, recibo.toString(), "Recibo", JOptionPane.INFORMATION_MESSAGE);
    }


    // Métodos auxiliares...

    private Cliente obterClienteParaCompra(List<Cliente> listaClientes) {
        // Listando todos os clientes com um índice
        StringBuilder clientesString = new StringBuilder("Escolha um cliente, se identifique inserindo um número inteiro que se encontra ao lado do nome\n");
        for (int i = 0; i < listaClientes.size(); i++) {
            Cliente cliente = listaClientes.get(i);
            clientesString.append("[").append(i).append("] ").append(cliente.getNome());
            if (cliente instanceof PessoaFisica) {
                clientesString.append(" (CPF: ").append(((PessoaFisica) cliente).getCpf()).append(")");
            } else if (cliente instanceof PessoaJuridica) {
                clientesString.append(" (CNPJ: ").append(((PessoaJuridica) cliente).getCnpj()).append(")");
            }
            clientesString.append("\n");
        }

        try {
            int escolhaCliente = Integer.parseInt(JOptionPane.showInputDialog(clientesString.toString()));
            if (escolhaCliente >= 0 && escolhaCliente < listaClientes.size()) {
                return listaClientes.get(escolhaCliente);
            } else {
                JOptionPane.showMessageDialog(null, "Índice inválido. Operação cancelada.", "Erro", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Entrada inválida. Operação cancelada.", "Erro", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }


    private List<ItemCompra> obterItensParaCompra(List<Produto> listaProdutos) {
        List<ItemCompra> itensCompra = new ArrayList<>();

        // Listando todos os produtos com um índice
        StringBuilder produtosString = new StringBuilder("Escolha os produtos (insira o número inteiro do produto que queira comprar):\n");
        for (int i = 0; i < listaProdutos.size(); i++) {
            Produto produto = listaProdutos.get(i);
            produtosString.append("[").append(i).append("] ").append(produto.getNomeProduto()).append(" - R$ ").append(produto.getValorUnitario()).append("\n");
        }

        try {
            String escolhaProdutos = JOptionPane.showInputDialog(produtosString.toString());

            // Verifica se o usuário cancelou a escolha
            if (escolhaProdutos == null) {
                return Collections.emptyList();
            }

            String[] indicesEscolhidos = escolhaProdutos.split(",");

            for (String indice : indicesEscolhidos) {
                int indiceProduto = Integer.parseInt(indice.trim());
                if (indiceProduto >= 0 && indiceProduto < listaProdutos.size()) {
                    Produto produtoEscolhido = listaProdutos.get(indiceProduto);
                    int quantidade = Integer.parseInt(JOptionPane.showInputDialog("Digite a quantidade para " + produtoEscolhido.getNomeProduto()));
                    itensCompra.add(new ItemCompra(quantidade, produtoEscolhido));
                } else {
                    JOptionPane.showMessageDialog(null, "Índice inválido. Operação cancelada.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return Collections.emptyList();
                }
            }

            return itensCompra;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Entrada inválida. Operação cancelada.", "Erro", JOptionPane.ERROR_MESSAGE);
            return Collections.emptyList();
        }
    }

    private Date obterDataCompra() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            String dataCompraStr = JOptionPane.showInputDialog("Digite a data da compra (Dia/Mês/Ano):");

            // Verifica se o usuário cancelou a entrada
            if (dataCompraStr == null) {
                return null;
            }

            return dateFormat.parse(dataCompraStr);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "Formato de data inválido. Operação cancelada.", "Erro", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private float calcularValorTotalCompra(List<ItemCompra> itensCompra) {
        float valorTotal = 0;
        for (ItemCompra item : itensCompra) {
            // Verifica se o produto associado ao ItemCompra não é nulo
            if (item.getProduto() != null) {
                valorTotal += item.getProduto().getValorUnitario() * item.getQuantidade();
            }
        }
        return valorTotal;
    }

    private int gerarIdentificadorCompra() {
        // Simples exemplo de geração de identificador. Pode ser ajustado conforme necessário.
        return listaCompras.size() + 1;
    }

    private void salvarCompraEmArquivo(Compra compra) {
        String pastaBaseDados = "C:\\GitHub\\Java\\TABAJARA\\baseDados";
        File arquivo = new File(pastaBaseDados, "compras.txt");

        try {
            if (!arquivo.getParentFile().exists()) {
                arquivo.getParentFile().mkdirs();
            }

            if (!arquivo.exists()) {
                arquivo.createNewFile();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo, false))) {
                // Escrevendo as informações da compra no arquivo
                writer.write("\n\n==== COMPRA ====\n");
                writer.write("Identificador: " + compra.getIdentificador() + "\n");
                writer.write("Data da Compra: " + new SimpleDateFormat("dd/MM/yyyy").format(compra.getDataCompra()) + "\n");

                // Exibindo CPF ou CNPJ do cliente
                if (compra.getCpf() != null) {
                    writer.write("Cliente (CPF): " + ((PessoaFisica) compra.getCpf()).getCpf() + "\n");
                } else if (compra.getCnpj() != null) {
                    writer.write("Cliente (CNPJ): " + ((PessoaJuridica) compra.getCnpj()).getCnpj() + "\n");
                }

                writer.write("Valor Total: R$" + compra.getValorTotal() + "\n");
                writer.write("---- Itens Comprados ----\n");

                // Ajuste para exibir detalhes dos itens comprados
                for (ItemCompra item : compra.getItensCompra()) {
                    writer.write("Nome do produto: " + item.getProduto().getNomeProduto() +"\n");
                    writer.write("Preço unitário: R$" +item.getProduto().getValorUnitario() + "\n");
                    writer.write("Quantidade: " + item.getQuantidade() + "\n");
                }

                writer.write("Total Pago: R$" + compra.getTotalPago() + "\n");
                writer.write("Falta Pagar: R$" + compra.getFaltaPagar() + "\n\n");

                JOptionPane.showMessageDialog(null, "Compra salva no arquivo 'compras.txt' com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar a compra no arquivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void atualizarArquivoCompras() {
        String pastaBaseDados = "C:\\GitHub\\Java\\TABAJARA\\baseDados";
        File arquivoOriginal = new File(pastaBaseDados, "compras.txt");

        try {
            if (!arquivoOriginal.getParentFile().exists()) {
                arquivoOriginal.getParentFile().mkdirs();
            }

            if (!arquivoOriginal.exists()) {
                arquivoOriginal.createNewFile();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoOriginal, false))) {
                for (Compra compra : listaCompras) {
                    // Escreva as informações da compra no arquivo, da mesma maneira que no método salvarCompraEmArquivo
                    writer.write("\n\n==== COMPRA ====\n");
                    writer.write("Identificador: " + compra.getIdentificador() + "\n");
                    writer.write("Data da Compra: " + new SimpleDateFormat("dd/MM/yyyy").format(compra.getDataCompra()) + "\n");

                    // Exibindo CPF ou CNPJ do cliente
                    if (compra.getCpf() != null) {
                        writer.write("Cliente (CPF): " + ((PessoaFisica) compra.getCpf()).getCpf() + "\n");
                    } else if (compra.getCnpj() != null) {
                        writer.write("Cliente (CNPJ): " + ((PessoaJuridica) compra.getCnpj()).getCnpj() + "\n");
                    }

                    writer.write("Valor Total: R$" + compra.getValorTotal() + "\n");
                    writer.write("---- Itens Comprados ----\n");

                    for (ItemCompra item : compra.getItensCompra()) {
                        writer.write("Nome do produto: " + item.getProduto().getNomeProduto() + "\n");
                        writer.write("Preço unitário: R$" +item.getProduto().getValorUnitario() + "\n");
                        writer.write("Quantidade: " + item.getQuantidade() + "\n");
                    }

                    writer.write("Total Pago: R$" + compra.getTotalPago() + "\n");
                    writer.write("Falta Pagar: R$" + compra.getFaltaPagar() + "\n\n");
                }

                JOptionPane.showMessageDialog(null, "Arquivo 'compras.txt' atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Erro ao atualizar o arquivo 'compras.txt': " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao criar o arquivo 'compras.txt': " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void carregarComprasDoArquivo() {
        String pastaBaseDados = "C:\\GitHub\\Java\\TABAJARA\\baseDados";
        File arquivo = new File(pastaBaseDados, "compras.txt");

        if (arquivo.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                String linha;

                while ((linha = reader.readLine()) != null) {
                    if (linha.startsWith("==== COMPRA ====")) {
                        Compra compra = new Compra();
                        List<ItemCompra> itensCompra = new ArrayList<>();
                        while ((linha = reader.readLine()) != null && !linha.isEmpty()) {
                            if (linha.startsWith("Identificador: ")) {
                                compra.setIdentificador(Integer.parseInt(linha.replace("Identificador: ", "")));
                            } else if (linha.startsWith("Data da Compra: ")) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                compra.setDataCompra(dateFormat.parse(linha.replace("Data da Compra: ", "").trim()));
                            } else if (linha.startsWith("Cliente (CPF): ")) {
                                // Verifica se é um cliente Pessoa Física
                                String cpf = linha.replace("Cliente (CPF): ", "").trim();

                                // Encontrar o cliente com base no CPF
                                Cliente clientePF = gerenciarCliente.encontrarClientePorCPF(cpf);
                                PessoaFisica pf = (PessoaFisica) clientePF;
                                if (clientePF != null) {
                                    compra.setCpf(pf);
                                } else {
                                    // Tratando o caso em que o cliente não é encontrado
                                    JOptionPane.showMessageDialog(null, "Cliente com CPF '" + cpf + "' não encontrado. Operação cancelada.", "Erro", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }  else if (linha.startsWith("Cliente (CNPJ): ")) {
                                // Verifica se é um cliente Pessoa Jurídica
                                String cnpj = linha.replace("Cliente (CNPJ): ", "").trim();

                                // Encontrar o cliente com base no CNPJ
                                Cliente clientePJ = gerenciarCliente.encontrarClientePessoaJuridicaPorCNPJ(cnpj);
                                PessoaJuridica pj = (PessoaJuridica) clientePJ;

                                if (clientePJ != null) {
                                    compra.setCnpj(pj);
                                } else {
                                    // Tratando o caso em que o cliente não é encontrado
                                    JOptionPane.showMessageDialog(null, "Cliente com CNPJ '" + cnpj + "' não encontrado. Operação cancelada.", "Erro", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            } else if (linha.startsWith("Valor Total: ")) {
                                compra.setValorTotal(Float.parseFloat(linha.replace("Valor Total: R$", "").trim()));
                            } else if (linha.startsWith("Nome do produto: ")) {
                                String nomeProduto = linha.replace("Nome do produto: ", "").trim();
                                float valorUnitario = Float.parseFloat(reader.readLine().replace("Preço unitário: R$", "").trim());

                                linha = reader.readLine();
                                int quantidade = Integer.parseInt(linha.replace("Quantidade: ", "").trim());

                                Produto produto = new Produto(nomeProduto, valorUnitario);
                                itensCompra.add(new ItemCompra(quantidade, produto));
                            } else if (linha.startsWith("Total Pago: ")) {
                                compra.setTotalPago(Float.parseFloat(linha.replace("Total Pago: R$", "").trim()));
                            } else if (linha.startsWith("Falta Pagar: ")) {
                                compra.setFaltaPagar(Float.parseFloat(linha.replace("Falta Pagar: R$", "").trim()));
                            }
                        }

                        // Adiciona a compra à lista
                        compra.setItensCompra(itensCompra);
                        listaCompras.add(compra);
                    }
                }
            } catch (IOException | ParseException e) {
                JOptionPane.showMessageDialog(null, "Erro ao carregar compras do arquivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void atualizarSituacaoPagamento() {
        // Obtendo o Identificador
        String identificadorStr = JOptionPane.showInputDialog("Digite o Identificador da compra:");

        // Verificando se o usuário cancelou a entrada
        if (identificadorStr == null) {
            return;
        }

        try {
            int identificador = Integer.parseInt(identificadorStr);

            // Buscando a compra
            Compra compra = encontrarCompraPorIdentificador(identificador);

            // Se a compra não foi encontrada, exibe mensagem e retorna
            if (compra == null) {
                JOptionPane.showMessageDialog(null, "Compra não encontrada. Verifique o Identificador e tente novamente.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Exibindo detalhes da compra
            exibirDetalhesCompra(compra);

            // Exibindo quanto falta pagar
            float faltaPagar = compra.getFaltaPagar();
            JOptionPane.showMessageDialog(null, "Falta Pagar: R$ " + String.format("%.2f", faltaPagar), "Situação de Pagamento", JOptionPane.INFORMATION_MESSAGE);

            // Solicitando valor para pagamento
            float valorPago = obterValorPago(faltaPagar);
            if (valorPago < 0) {
                return;  // Usuário cancelou a entrada
            }

            // Atualizando valores na compra
            float novoTotalPago = compra.getTotalPago() + valorPago;
            float novoFaltaPagar = faltaPagar - valorPago;

            compra.setTotalPago(novoTotalPago);
            compra.setFaltaPagar(novoFaltaPagar);

            // Atualizando no arquivo
            atualizarArquivoCompras();

            JOptionPane.showMessageDialog(null, "Pagamento registrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Identificador inválido. Operação cancelada.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exibirDetalhesCompra(Compra compra) {
        StringBuilder detalhesCompra = new StringBuilder("Detalhes da Compra:\n");

        detalhesCompra.append("Identificador: ").append(compra.getIdentificador()).append("\n");
        detalhesCompra.append("Data da Compra: ").append(new SimpleDateFormat("dd/MM/yyyy").format(compra.getDataCompra())).append("\n");

        // Exibindo CPF ou CNPJ do cliente
        if (compra.getCpf() != null) {
            detalhesCompra.append("Cliente (CPF): ").append(compra.getCpf().getNome()).append(" - ").append(((PessoaFisica) compra.getCpf()).getCpf()).append("\n");
        } else if (compra.getCnpj() != null) {
            detalhesCompra.append("Cliente (CNPJ): ").append(compra.getCnpj().getNome()).append(" - ").append(((PessoaJuridica) compra.getCnpj()).getCnpj()).append("\n");
        }

        detalhesCompra.append("Valor Total da Compra: R$ ").append(String.format("%.2f", compra.getValorTotal())).append("\n");

        // Exibindo detalhes dos itens comprados
        detalhesCompra.append("Itens Comprados:\n");
        for (ItemCompra item : compra.getItensCompra()) {
            detalhesCompra.append("  - ").append(item.getQuantidade()).append("x ").append(item.getProduto().getNomeProduto())
                    .append(" - R$ ").append(String.format("%.2f", item.getProduto().getValorUnitario())).append("\n");
        }

        detalhesCompra.append("Total Pago: R$ ").append(String.format("%.2f", compra.getTotalPago())).append("\n");
        detalhesCompra.append("Falta Pagar: R$ ").append(String.format("%.2f", compra.getFaltaPagar())).append("\n");

        JOptionPane.showMessageDialog(null, detalhesCompra.toString(), "Detalhes da Compra", JOptionPane.INFORMATION_MESSAGE);
    }


    private Compra encontrarCompraPorIdentificador(int identificador) {
        for (Compra compra : listaCompras) {
            if (compra.getIdentificador() == identificador) {
                return compra;
            }
        }
        return null; // Retorna null se não encontrar a compra
    }

    private float obterValorPago(float faltaPagar) {
        try {
            String valorPagoStr = JOptionPane.showInputDialog("Digite o valor que deseja pagar (obs: insira com ponto ao invés da vírgula):");

            // Verificar se o usuário cancelou a entrada
            if (valorPagoStr == null) {
                return -1;
            }

            float valorPago = Float.parseFloat(valorPagoStr);

            // Verificar se o valor pago é válido
            while (valorPago < 0 || valorPago > faltaPagar) {
                valorPagoStr = JOptionPane.showInputDialog("Valor excedeu o valor total a pagar. Digite novamente o valor que deseja pagar:");

                // Verificar se o usuário cancelou a entrada
                if (valorPagoStr == null) {
                    return -1;
                }

                valorPago = Float.parseFloat(valorPagoStr);
            }

            return valorPago;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Entrada inválida. Operação cancelada.", "Erro", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    public List<Compra> getListaCompras() {
        return this.listaCompras;
    }

    public void exibirListaComprasSimplificada(List<Compra> compras) {
        if (compras.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Não há compras registradas.", "Lista de Compras", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder mensagem = new StringBuilder("Lista de Compras:\n");

            for (Compra compra : compras) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String dataCompraStr = (compra.getDataCompra() != null) ? dateFormat.format(compra.getDataCompra()) : "N/A";

                mensagem.append("Compra ").append(compra.getIdentificador()).append(" - ").append(dataCompraStr)
                        .append(" - R$").append(compra.getValorTotal()).append("\n");
            }

            JOptionPane.showMessageDialog(null, mensagem.toString(), "Lista de Compras", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // (f) Busca de uma compra pelo número;
    public Compra buscarCompraPorNumero(int numero) {
        for (Compra compra : getListaCompras()) {
            if (compra.getIdentificador() == numero) {
                return compra;
            }
        }
        return null; // Compra não encontrada
    }

    public void exibirListaComprasSimplificadaComEscolha(List<Compra> compras) {
        if (compras.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Não há compras registradas.", "Lista de Compras", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder mensagem = new StringBuilder("Lista de Compras:\n");

            for (Compra compra : compras) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String dataCompraStr = (compra.getDataCompra() != null) ? dateFormat.format(compra.getDataCompra()) : "N/A";

                mensagem.append("Compra ").append(compra.getIdentificador()).append(" - ").append(dataCompraStr)
                        .append(" - R$").append(compra.getValorTotal()).append("\n");
            }

            // Solicitar que a pessoa insira o identificador da compra
            String idCompraStr = JOptionPane.showInputDialog(null, mensagem.toString() +
                    "\n\nDigite o identificador da compra para ver os detalhes:", "Escolha o Identificador", JOptionPane.QUESTION_MESSAGE);

            // Verificar se a pessoa fechou a caixa de diálogo ou cancelou
            if (idCompraStr == null || idCompraStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Operação cancelada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            try {
                int idEscolhido = Integer.parseInt(idCompraStr);
                Compra compraEscolhida = buscarCompraPorNumero(idEscolhido);

                if (compraEscolhida != null) {
                    exibirDetalhesCompra(compraEscolhida);
                } else {
                    JOptionPane.showMessageDialog(null, "Compra não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Formato de identificador inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // (g) Relação de todas as compras que não foram pagas ainda;
    public List<Compra> listarComprasNaoPagas() {
        List<Compra> comprasNaoPagas = new ArrayList<>();
        for (Compra compra : getListaCompras()) {
            if (compra.getFaltaPagar() > 0) {
                comprasNaoPagas.add(compra);
            }
        }
        return comprasNaoPagas;
    }

    public void exibirComprasNaoPagas() {
        List<Compra> comprasNaoPagas = listarComprasNaoPagas();

        if (comprasNaoPagas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Todas as compras estão pagas.", "Compras Pagas", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder mensagem = new StringBuilder("Compras não pagas:\n");

            for (Compra compra : comprasNaoPagas) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String dataCompraStr = (compra.getDataCompra() != null) ? dateFormat.format(compra.getDataCompra()) : "N/A";

                mensagem.append("Compra ").append(compra.getIdentificador()).append(" - ").append(dataCompraStr)
                        .append(" - R$").append(compra.getValorTotal()).append("\n");
            }

            JOptionPane.showMessageDialog(null, mensagem.toString(), "Compras Não Pagas", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // (h) Relação das 10 últimas compras pagas;
    public List<Compra> listarUltimas10ComprasPagas() {
        List<Compra> comprasPagas = new ArrayList<>();
        List<Compra> todasCompras = getListaCompras();
        // Filtrar apenas as compras pagas
        todasCompras.stream()
                .filter(compra -> compra.getTotalPago() == compra.getValorTotal())
                .sorted(Comparator.comparing(Compra::getDataCompra).reversed())
                .limit(10)
                .forEach(comprasPagas::add);
        return comprasPagas;
    }

    public void exibirUltimas10ComprasPagas() {
        List<Compra> ultimas10ComprasPagas = listarUltimas10ComprasPagas();

        if (ultimas10ComprasPagas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Não há compras pagas registradas.", "Compras Pagas", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder mensagem = new StringBuilder("Últimas 10 compras pagas:\n");

            for (Compra compra : ultimas10ComprasPagas) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String dataCompraStr = (compra.getDataCompra() != null) ? dateFormat.format(compra.getDataCompra()) : "N/A";

                mensagem.append("Compra ").append(compra.getIdentificador()).append(" - ").append(dataCompraStr)
                        .append(" - R$").append(compra.getValorTotal()).append("\n");
            }

            JOptionPane.showMessageDialog(null, mensagem.toString(), "Últimas 10 Compras Pagas", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // (i) Apresentação das informações da compra mais cara;
    public Compra obterCompraMaisCara() {
        return getListaCompras().stream()
                .max(Comparator.comparing(Compra::getValorTotal))
                .orElse(null);
    }

    public void exibirCompraMaisCara() {
        Compra compraMaisCara = obterCompraMaisCara();

        if (compraMaisCara != null) {
            StringBuilder mensagem = new StringBuilder("Informações da compra mais cara:\n");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dataCompraStr = (compraMaisCara.getDataCompra() != null) ? dateFormat.format(compraMaisCara.getDataCompra()) : "N/A";

            mensagem.append("Identificador: ").append(compraMaisCara.getIdentificador()).append("\n");
            mensagem.append("Data da Compra: ").append(dataCompraStr).append("\n");
            mensagem.append("Cliente: ").append(obterNomeCliente(compraMaisCara)).append("\n");
            mensagem.append("Valor Total da Compra: R$ ").append(String.format("%.2f", compraMaisCara.getValorTotal())).append("\n");

            JOptionPane.showMessageDialog(null, mensagem.toString(), "Compra Mais Cara", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Não há compras registradas.", "Compra Mais Cara", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String obterNomeCliente(Compra compra) {
        if (compra.getCpf() != null) {
            return ((PessoaFisica) compra.getCpf()).getNome();
        } else if (compra.getCnpj() != null) {
            return ((PessoaJuridica) compra.getCnpj()).getNome();
        } else {
            return "N/A";
        }
    }

      // (j) Apresentação das informações da compra mais barata;
      public Compra obterCompraMaisBarata() {
        return getListaCompras().stream()
                .filter(compra -> compra.getFaltaPagar() == 0) // Considerar apenas compras pagas
                .min(Comparator.comparing(Compra::getValorTotal))
                .orElse(null);
    }

    public void exibirCompraMaisBarata() {
        Compra compraMaisBarata = obterCompraMaisBarata();

        if (compraMaisBarata != null) {
            StringBuilder mensagem = new StringBuilder("Informações da compra mais barata:\n");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dataCompraStr = (compraMaisBarata.getDataCompra() != null) ? dateFormat.format(compraMaisBarata.getDataCompra()) : "N/A";

            mensagem.append("Identificador: ").append(compraMaisBarata.getIdentificador()).append("\n");
            mensagem.append("Data da Compra: ").append(dataCompraStr).append("\n");
            mensagem.append("Cliente: ").append(obterNomeCliente(compraMaisBarata)).append("\n");
            mensagem.append("Valor Total da Compra: R$ ").append(String.format("%.2f", compraMaisBarata.getValorTotal())).append("\n");

            JOptionPane.showMessageDialog(null, mensagem.toString(), "Compra Mais Barata", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Não há compras registradas ou todas estão pendentes de pagamento.", "Compra Mais Barata", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // (k) Relação do valor total de compras feitas em cada mês nos últimos 12 meses.
    public List<Double> calcularValorTotalComprasPorMes() {
        List<Double> valoresPorMes = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -12); // Retrocedendo 12 meses a partir do momento atual

        for (int i = 0; i < 12; i++) {
            calendar.add(Calendar.MONTH, 1);
            Date inicioMes = calendar.getTime();
            calendar.add(Calendar.MONTH, 1);
            Date fimMes = calendar.getTime();

            double totalComprasMes = getListaCompras().stream()
                    .filter(compra -> compra.getDataCompra().after(inicioMes) && compra.getDataCompra().before(fimMes))
                    .mapToDouble(Compra::getValorTotal)
                    .sum();

            valoresPorMes.add(totalComprasMes);
        }

        return valoresPorMes;
    }

    public void exibirValorTotalComprasPorMes() {
        List<Double> valoresPorMes = calcularValorTotalComprasPorMes();

        if (!valoresPorMes.isEmpty()) {
            StringBuilder mensagem = new StringBuilder("Valor total de compras nos últimos 12 meses:\n");

            SimpleDateFormat monthFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();

            for (int i = 0; i < 12; i++) {
                calendar.add(Calendar.MONTH, -1);
                Date inicioMes = calendar.getTime();
                calendar.add(Calendar.MONTH, 1);
                Date fimMes = calendar.getTime();

                String nomeMes = monthFormat.format(inicioMes);
                double totalComprasMes = valoresPorMes.get(i);

                mensagem.append(nomeMes).append(": R$ ").append(String.format("%.2f", totalComprasMes)).append("\n");
            }

            JOptionPane.showMessageDialog(null, mensagem.toString(), "Valor Total de Compras por Mês", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Não há compras registradas nos últimos 12 meses.", "Valor Total de Compras por Mês", JOptionPane.INFORMATION_MESSAGE);
        }
    }


}
