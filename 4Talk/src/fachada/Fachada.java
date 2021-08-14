package fachada;

import java.util.ArrayList;
import java.util.List;

import dao.DAO;
import dao.DAOLog;
import dao.DAOMensagem;
import dao.DAOUsuario;
import modelo.Log;
import modelo.Mensagem;
import modelo.Usuario;

public class Fachada {
	private static DAOUsuario daousuario = new DAOUsuario();
	private static DAOMensagem daomensagem = new DAOMensagem();
	private static DAOLog daolog = new DAOLog();

	private static Usuario usuariologado=null;


	public static void inicializar() {
		DAO.open();
	}

	public static void finalizar(){
		DAO.close();
	}

	public static List<Usuario> listarUsuarios() {
		// nao precisa estar logado
		return daousuario.readAll();	
	}
	public static List<Mensagem> listarMensagens() {
		// nao precisa estar logado
		return daomensagem.readAll();	
	}

	public static List<Log> listarLogs() {
		// nao precisa estar logado
		return daolog.readAll();	
	}
	public static List<Mensagem> buscarMensagens(String termo) throws  Exception{
		/*
		 * nao precisa estar logado
		 * query no banco para obter mensagens do grupo que contenha
		 *  o termo (considerar case insensitive)
		 * 
		 */
		return daomensagem.readByTermo(termo);
	}

	public static Usuario criarUsuario(String nome, String senha) throws  Exception{
		// nao precisa estar logado
		DAO.begin();	
		Usuario u = daousuario.read(nome+"/"+senha);	
		if(u != null) {
			DAO.rollback();	
			throw new Exception("criar usuario - usuario existente:" + nome);
		}

		u = new Usuario(nome+"/"+senha);
		daousuario.create(u);		
		DAO.commit();
		return u;
	}


	public static void login(String nome, String senha) throws Exception{		
		//verificar se ja existe um usuario logada
		if(usuariologado!=null)
			throw new Exception ("ja existe um usuario logado"+getLogado());
		DAO.begin();
		Usuario u = daousuario.read(nome+"/"+senha);	
		if(u == null) {
			DAO.rollback();	
			throw new Exception("login - usuario inexistente:" + nome);
		}
		if(!u.ativo()) {
			DAO.rollback();	
			throw new Exception("login - usuario nao ativo:" + nome);
		}
		usuariologado = u;		//altera o logado na fachada

		Log log = new Log(usuariologado.getNome());
		daolog.create(log);
		DAO.commit();
	}
	public static void logoff() {		
		usuariologado = null; 		//altera o logado na fachada
	}

	public static Usuario getLogado() {
		return usuariologado;
	}



	public static Mensagem criarMensagem(String texto) throws Exception{
		/*
		 * tem que esta logado
		 * criar a mensagem, onde o criador � a usuario logada
		 * adicionar esta mensagem na lista de mensagens de cada usuario do grupo,
		 * incluindo a do criador
		 * retornar mensagem criada
		 */

		//para gerar o novo id da mensagem utilize:

		DAO.begin();
		if(usuariologado != null) {
			int id = daomensagem.obterUltimoId();
			id++;
			Mensagem m = new Mensagem(id, usuariologado, texto);
			daomensagem.create(m);
			DAO.commit();
			return m;
		}
		DAO.rollback();
		throw new Exception("É preciso estar logado para enviar uma mensagem.");
	}



	public static List<Mensagem> listarMensagensUsuario() throws Exception{
		/*
		 * tem que esta logado
		 * retorna todas as mensagens do usuario logado
		 * 
		 */
		if(usuariologado != null){
			return daomensagem.getMensagensUsuario(usuariologado);
		}
		throw new Exception("É necessário estar logado para visualizar as mensagens");

	}


	public static void apagarMensagens(int... ids) throws  Exception{
		/*
		 * tem que esta logado
		 * recebe uma lista de numeros de id 
		 * (id � um numero entre 1 a N, onde N � a quatidade atual de mensagens do grupo)
		 * validar se ids s�o de mensagens criadas pelo usuario logado
		 * (um usuario nao pode apagar mensagens de outros usuarios)
		 * 
		 * remover cada mensagem da lista de mensagens do usuario logado
		 * apagar cada mensagem do banco 
		 */
		if(ids.length<0){
			throw new Exception("Nenhum ID informado, por favor insira um número maior que 0");
		}
		DAO.begin();
		if(usuariologado != null){
			List<Mensagem> mensagens = listarMensagens();
			List<Mensagem> mensagensUsuario = listarMensagensUsuario();
			List<Mensagem> paraApagar = new ArrayList<Mensagem>();

			for(Mensagem mensagem:mensagens) {
				for (int id : ids) {
					if (mensagem.getId() == id && usuariologado == mensagem.getCriador()) {
						paraApagar.add(mensagem);
					}
				}
			}
			if(paraApagar.size()>0){
				for(Mensagem mensagem : paraApagar){
					usuariologado.remover(mensagem);
					daomensagem.delete(mensagem);
				}
			}else {
				DAO.rollback();
				throw new Exception("As mensagens pertencem a outro usuário");
			}
			DAO.commit();
		}else {
			DAO.rollback();
			throw new Exception("É necessário estar logado para apagar mensagens");
		}
	}

	public static void sairDoGrupo() throws  Exception{
		/*
		 * tem que esta logado
		 * 
		 * criar a mensagem "fulano saiu do grupo"
		 * desativar o usuario logado e fazer logoff dele
		 */
		if(usuariologado != null){
			usuariologado.desativar();
			criarMensagem(usuariologado.getNome() + " saiu do grupo");
			logoff();
		}else {
			throw new Exception("É necessário estar logado para sair do grupo");
		}
	}


//	public static int totalMensagensUsuario() throws Exception{
//		/*
//		 * tem que esta logado
//		 * retorna total de mensagens criadas pelo usuario logado
//		 * 
//		 */
//	}

	public static void esvaziar() throws Exception{
		DAO.clear();
	}

}

