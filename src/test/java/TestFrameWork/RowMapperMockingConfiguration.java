package TestFrameWork;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.PreparedStatement;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PowerMockRunnerDelegate(BlockJUnit4ClassRunner.class)
@PrepareForTest({DataSourceUtils.class, PreparedStatement.class})
@Ignore
public class RowMapperMockingConfiguration {
}
