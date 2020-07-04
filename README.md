# spring-boot-rsql-demo
Study project for REST query language with RSQL and spring boot
## The problem and motivation
Find a better way to implement server side filtering, using a syntax convention for the query parameters on a GET request.
In this example, I am querying the Entity database for one instance named `ENTITY_1`, with email `ONE@ANYMAIL.COM`.

<pre><code>GET http://localhost:8080/entity?name==ENTITY_1;email==ONE@ANYMAIL.COM.
</code></pre>

Using a standard approach, I would have to write a controller to receive this request, and parse the query parameters, usually with a regex, in order to create a `Specification` object that would be used to query the database.

Example controller:
<pre><code>
    @RequestMapping(method = RequestMethod.GET, value = "/users")
    @ResponseBody
    public List<User> findAll(@RequestParam(value = "search", required = false) String search) {
        List<SearchCriteria> params = new ArrayList<SearchCriteria>();
        if (search != null) {
            Pattern pattern = Pattern.compile("(\w+?)(:|<|>)(\w+?),");
            Matcher matcher = pattern.matcher(search + ",");
            while (matcher.find()) {
                params.add(new SearchCriteria(matcher.group(1), 
                  matcher.group(2), matcher.group(3)));
            }
        }
        return api.searchUser(params);
    }</code></pre>
    
The search criteria would then be processed in order to create a `Specification`. This means a lof of work envolving null and operator checks. So, I began looking for ways to make it easier.
I tried out some libraries would help with a syntax convention for the query parameters on the request:

* [rsql-parser](https://github.com/jirutka/rsql-parser).
* [rsql-jpa](https://github.com/tennaito/rsql-jpa).
* [rsql-jpa-specification](https://github.com/perplexhub/rsql-jpa-specification).

## Conclusion:

My comments, and implementations, using the libraries can also be found on the `RsqlTests` class under the test package.

* <ins>rsql-parser</ins>: Basic abstraction for parsing the query parameters on a HTTP request. Depends on an additional layer on top of it in order to create a JPA Specification from the parsed AST. The most verbose of the alternatives presented.
* <ins>rsql-jpa</ins>: Abstraction layer on top of rsql-parser. The main downside is having to expose the EntityManager. This method is the most flexible, since you can change the CriteriaQuery before executing it. You can change the sorting of the query, for example, before creating it with the EntityManager.
* <ins>rsql-jpa-specification</ins>: Another abstraction layer on top of rsql-parser. This is the most straightforward one. All you need to do is to call one method in order to execute a simple query. No need to expose an EntityManager. The main downside is the loss of flexibility when comparing to the previous method. This is the one lib preferred for apps with simple queries.
