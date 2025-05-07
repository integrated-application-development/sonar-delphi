unit BestEffortArguments;

interface

implementation

procedure Foo;
var
  Bar: Integer;
begin
  Baz(Bar)[Bar]; // The Baz invocation does not resolve, but the Bar expressions still should.
end;

end.