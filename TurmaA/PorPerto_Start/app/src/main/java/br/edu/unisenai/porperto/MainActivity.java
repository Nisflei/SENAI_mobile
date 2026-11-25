package br.edu.unisenai.porperto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import br.edu.unisenai.porperto.model.Comercio;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "PorPerto";
    private LinearProgressIndicator progresso;

    private GoogleMap mapa;
    private LatLng minhaPosicao;

    private PlacesClient places;
    private List<Place.Field> CAMPOS = Arrays.asList(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION
    );
    private List<Comercio> listaComercio = new ArrayList<>();



    // config Permissao
    private ActivityResultLauncher<String[]> solicitarPermissao =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    resposta -> {
                        boolean exata = Boolean.TRUE.equals(resposta.get(Manifest.permission.ACCESS_FINE_LOCATION));
                        boolean aproximada = Boolean.TRUE.equals(resposta.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                        if (exata || aproximada ){
                            Log.i("Permissao", "Permissao Concedida");
                        } else {
                            Toast.makeText(this, "Permissão Negada", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    private boolean temPermissao(){
        return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    // Fim config Permissao

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Componentes
        progresso = findViewById(R.id.progresso);
        FloatingActionButton fabMinhaPosicao = findViewById(R.id.fabMinhaPosicao);
        ChipGroup chipsCategorias = findViewById(R.id.chipsCategorias);

        // Verificar permissão
        verificarPermissao();

        // Ativar Mapa na tela
        SupportMapFragment mapaTela = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapaTela.getMapAsync(this);

        fabMinhaPosicao.setOnClickListener(v -> {
            ativarPosicao();
        });

        // Obter o tipo de comercio
        chipsCategorias.setOnCheckedStateChangeListener((chipGroup, indicado) -> {
            if (indicado.isEmpty()) return;

            Chip item = chipGroup.findViewById(indicado.get(0));
            buscarLugares(item.getTag().toString());
        });

    }

    private void buscarLugares(String comercio) {
        if (mapa == null) return;

        LatLng centroPesquisa = mapa.getCameraPosition().target;
        progresso.setVisibility(View.VISIBLE);

        // Inicializar os parametros para API de Busca
        if (!Places.isInitialized()){
            Places.initialize(getApplicationContext(),BuildConfig.MAPS_API_KEY);
        }
        places = Places.createClient(this);
        CircularBounds raioPesquisa = CircularBounds.newInstance(centroPesquisa, 1500);
        // FIM

        SearchNearbyRequest requisicao = SearchNearbyRequest.builder(raioPesquisa, CAMPOS)
                .setIncludedTypes(Collections.singletonList(comercio))
                .setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
                .setRegionCode("BR")
                .build();

        places.searchNearby(requisicao)
                .addOnSuccessListener(response -> {
                   listaComercio.clear();
                   for (Place lugar: response.getPlaces()){
                       listaComercio.add(new Comercio(lugar.getId()
                               ,lugar.getDisplayName()
                               ,lugar.getFormattedAddress()
                               ,lugar.getLocation().latitude
                               ,lugar.getLocation().longitude
                       ));
                   }
                   // Carregar no MAPA - Marcadores
                   mapa.clear();
                   for (Comercio item: listaComercio ){
                       Marker point = mapa.addMarker(new MarkerOptions()
                                        .title(item.getNome())
                                        .snippet(item.getEndereco())
                                        .position(new LatLng(item.getLatitude(),item.getLongitude()))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                       );
                       point.setTag(item);
                   }




                })
                .addOnFailureListener(e -> {
                   Toast.makeText(this,"Erro ao localizar comercio!!",Toast.LENGTH_SHORT).show();
                });

    }

    private void ativarPosicao() {
        progresso.setVisibility(View.VISIBLE);

        FusedLocationProviderClient meuGPS = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mapa.setMyLocationEnabled(true);
        meuGPS.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    progresso.setVisibility(View.INVISIBLE);
                    minhaPosicao = new LatLng(location.getLatitude(), location.getLongitude());
                    mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(minhaPosicao, 15));
                })
                .addOnFailureListener(e -> {
                    progresso.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Deu ruim...GPS !!!", Toast.LENGTH_SHORT).show();
                });
    }

    private void verificarPermissao() {
        if (!temPermissao()){
            solicitarPermissao.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.getUiSettings().setZoomControlsEnabled(true);
        mapa.getUiSettings().setMyLocationButtonEnabled(false);

        mapa.setOnInfoWindowClickListener(marker -> {
            Comercio item = (Comercio) marker.getTag();

            if (item != null){
                // Abrir o GoogleMaps
                Uri endereco = new Uri.Builder()
                        .scheme("https")
                        .authority("www.google.com")
                        .path("maps/dir/")
                        .appendQueryParameter("api","1")
                        .appendQueryParameter("destination",
                                String.format(Locale.US,"%.6f,%.6f", item.getLatitude(),item.getLongitude()))
                        .build();

                Intent maps = new Intent(Intent.ACTION_VIEW, endereco);
                startActivity(maps);
            }
        });

    }
}
