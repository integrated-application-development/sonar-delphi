unit UnitScopeName.ScopedUnit3;

interface

procedure ScopedUnit3Proc(Argument: String);

implementation

uses
    UnitScopeNameTest
  , Unit2
  ;

procedure ScopedUnit3Proc(Argument: String);
begin
  // Do nothing
end;

end.