package dao;

import com.db4o.query.Query;
import modelo.Log;
import modelo.Usuario;

import java.util.List;

public class DAOLog extends DAO<Log>{

    public Log read(Object chave) {
        String datahora = (String) chave;
        Query q = manager.query();
        q.constrain(Usuario.class);
        q.descend("datahora").constrain(datahora);
        List<Log> resultados = q.execute();
        if (resultados.size()>0)
            return resultados.get(0);
        else
            return null;

    }
}
