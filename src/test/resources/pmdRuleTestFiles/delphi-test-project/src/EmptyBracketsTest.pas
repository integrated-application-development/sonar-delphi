unit EmptyBracketsTest;

interface


implementation

begin
// Noncompliant
  if IsEmpty() then
  begin
    writeln ('Hello, world.');
  end;

// Compliant
  if IsEmpty then
  begin
    writeln ('Hello, world.');
  end;

end.
