package br.edu.unisenai.chefdebolso.ia;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerativeBackend;

import java.util.concurrent.Executor;

import br.edu.unisenai.chefdebolso.Config;

// definir comportamento de uso da IA
public class ChefIA {

    private Executor executorPrincipal;
    private GenerativeModelFutures modeloChat;
    private GenerativeModelFutures modeloReceita;

    public ChefIA(Context contexto){

        executorPrincipal = ContextCompat.getMainExecutor(contexto);
        FirebaseAI ai = FirebaseAI.getInstance(GenerativeBackend.googleAI());

        // 1o. passo: definir persona do chat
        Content instrucaoChef = new Content.Builder()
                .addText(Config.INSTRUCAO_CHEF)
                .build();
        GenerativeModel chef = ai.generativeModel(Config.MODELO,null,null,null,null,instrucaoChef);
        modeloChat = GenerativeModelFutures.from(chef);
    }
}
