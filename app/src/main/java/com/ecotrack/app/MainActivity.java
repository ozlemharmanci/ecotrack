package com.ecotrack.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ecotrack.app.fragments.GunlukFragment;
import com.ecotrack.app.fragments.KesfetFragment;
import com.ecotrack.app.fragments.PanelFragment;
import com.ecotrack.app.fragments.ProfilFragment;
import com.ecotrack.app.utils.AktiviteEkleDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Uygulamanın tek Activity'si.
 *
 * Modern Android mimarisinde "tek Activity + çoklu Fragment" yaklaşımı tercih
 * edilir. Alt navigasyon çubuğundaki her sekme bir Fragment gösterir:
 *   - Panel   -> PanelFragment   (1. ekran)
 *   - Günlük  -> GunlukFragment  (2. ekran)
 *   - Keşfet  -> KesfetFragment  (3. ekran, API'den haber çeker)
 *   - Profil  -> ProfilFragment
 *
 * Sağ altta + (FAB) düğmesi tüm sekmelerde görünür. Tıklanınca aktivite
 * ekleme diyalogu açılır. Aktivite eklendikten sonra mevcut fragment
 * yeniden yüklenir, listeler güncellenir.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);
        FloatingActionButton fab = findViewById(R.id.aktiviteEkleFab);

        // İlk açılışta Panel sekmesini göster
        if (savedInstanceState == null) {
            fragmentDegistir(new PanelFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment secilen = null;
            int id = item.getItemId();

            if (id == R.id.nav_panel) {
                secilen = new PanelFragment();
            } else if (id == R.id.nav_gunluk) {
                secilen = new GunlukFragment();
            } else if (id == R.id.nav_kesfet) {
                secilen = new KesfetFragment();
            } else if (id == R.id.nav_profil) {
                secilen = new ProfilFragment();
            }

            if (secilen != null) {
                fragmentDegistir(secilen);
                return true;
            }
            return false;
        });

        // FAB (+) tıklama - aktivite ekleme diyalogunu aç
        fab.setOnClickListener(v -> {
            AktiviteEkleDialog.goster(this, yeniAktivite -> {
                // Aktivite eklendi - kullanıcıya geri bildirim
                Toast.makeText(this,
                        yeniAktivite.getTitle() + " eklendi (+" + yeniAktivite.getXpReward() + " XP)",
                        Toast.LENGTH_SHORT).show();

                // Mevcut fragment'ı yeniden yükle ki listeler/sayılar güncellensin
                aktifFragmentiYenile();
            });
        });
    }

    /** Verilen fragment'ı içerik konteynerine yerleştirir */
    private void fragmentDegistir(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /**
     * Aktivite eklendikten sonra çağrılır. SADECE Panel ve Günlük sekmeleri
     * DB'ye bağımlı olduğu için onlar yenilenir. Keşfet'te arama/kategori
     * durumu kaybolmasın diye dokunulmaz.
     */
    private void aktifFragmentiYenile() {
        int aktifId = bottomNav.getSelectedItemId();
        if (aktifId == R.id.nav_panel) {
            fragmentDegistir(new PanelFragment());
        } else if (aktifId == R.id.nav_gunluk) {
            fragmentDegistir(new GunlukFragment());
        }
        // Keşfet ve Profil DB'ye bağımlı değil - dokunma
    }
}
