package dao;

import com.db4o.query.Query;
import modelo.Log;
import modelo.Mensagem;
import modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

public class DAOMensagem extends DAO<Mensagem>{

    public Mensagem read(Object chave) {
        String id = (String) chave;
        Query q = manager.query();
        q.constrain(Usuario.class);
        q.descend("id").constrain(id);
        List<Mensagem> resultados = q.execute();
        if (resultados.size()>0)
            return resultados.get(0);
        else
            return null;

    }

    public  List<Mensagem> readByTermo(String termo) {
        Query q = manager.query();
        q.constrain(Mensagem.class);
        termo = termo.toLowerCase();
        List<Mensagem> mensagens = q.execute();
        List<Mensagem> result = new ArrayList<Mensagem>();
        if(mensagens.size() > 0){
            for(Mensagem mensagem : mensagens) {
                String texto = mensagem.getTexto().toLowerCase();
                if (texto.contains(termo)) {
                   result.add(mensagem);
                }
            }
        }
        return result;
    }

    public List<Mensagem> getMensagensUsuario(Usuario criador){
        Query q = manager.query();
        q.constrain(Mensagem.class);
        List<Mensagem> mensagens = q.execute();
        List<Mensagem> resultados = new ArrayList<Mensagem>();
        if(mensagens.size() > 0){
            for(Mensagem mensagem : mensagens) {
                if (mensagem.getCriador().equals(criador)) {
                    resultados.add(mensagem);
                }
           }
        }
        return resultados;
    }
}
