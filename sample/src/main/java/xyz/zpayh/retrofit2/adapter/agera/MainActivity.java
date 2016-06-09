package xyz.zpayh.retrofit2.adapter.agera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.agera.Repositories;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.Updatable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class MainActivity extends AppCompatActivity implements Updatable {

    private TextView mTextView;

    private Repository<String> mRepository;

    private Executor mNetworkExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        /*mRepository = Repositories.repositoryWithInitialValue("")
                .observe()
                .onUpdatesPerLoop()
                .goTo(mNetworkExecutor)
                //.thenAttemptGetFrom(getGitHubService().listRepos1("EvilBT"))
                .thenAttemptGetFrom(getGitHubService().listRepos2("EvilBT"))
                .orSkip()
                .compile();*/

        mRepository = Repositories.repositoryWithInitialValue("")
                .observe()
                .onUpdatesPerLoop()
                .goTo(mNetworkExecutor)
                .attemptGetFrom(getGitHubService().listRepos2("EvilBT"))
                //.attemptGetFrom(getGitHubService().listRepos1("EvilBT"))
                .orSkip()
                .thenAttemptTransform(response -> {
                    if (response.isSuccessful()){
                        return Result.success(response.body());
                    }
                    return Result.failure();
                }).orSkip()
                .compile();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mRepository.addUpdatable(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRepository.removeUpdatable(this);
    }

    private void initView() {
        mTextView = (TextView) findViewById(R.id.text);
    }

    @Override
    public void update() {
        mTextView.setText(mRepository.get());
    }


    GitHubService getGitHubService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(AgeraCallAdapterFactory.create())
                .build();

        return retrofit.create(GitHubService.class);
    }

    interface GitHubService {
        //@GET("users/{user}/repos")
        //Supplier<Result<String>> listRepos1(@Path("user")String user);

        @GET("users/{user}/repos")
        Supplier<Result<Response<String>>> listRepos1(@Path("user")String user);

        @GET("users/{user}/repos")
        SupplierResult<Response<String>> listRepos2(@Path("user")String user);

        //@GET("users/{user}/repos")
        //SupplierResult<String> listRepos2(@Path("user")String user);
    }
}
