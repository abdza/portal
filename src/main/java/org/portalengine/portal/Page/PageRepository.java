package portal.Page;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRepository extends JpaRepository<Page, Long> {

	Page findOneByModuleAndSlug(String module,String slug);
}
